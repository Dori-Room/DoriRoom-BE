package doritos.doriroom.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import doritos.doriroom.challenge.domain.challenge.Challenge;
import doritos.doriroom.challenge.repository.ChallengeRepository;
import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.domain.EventDetailStatus;
import doritos.doriroom.event.dto.request.EventItemFilterRequestDto;
import doritos.doriroom.event.dto.response.EventDetailResponseDto;
import doritos.doriroom.event.dto.response.EventResponseDto;
import doritos.doriroom.event.exception.EventNotFoundException;
import doritos.doriroom.global.cache.RedisCacheService;
import doritos.doriroom.tourApi.dto.response.TourApiDetailInfoDto;
import doritos.doriroom.tourApi.dto.response.TourApiDetailIntroDto;
import doritos.doriroom.tourApi.dto.response.TourApiItemDto;
import doritos.doriroom.tourApi.service.TourApiService;
import doritos.doriroom.event.repository.EventRepository;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventService {
    private final TourApiService tourApiService;
    private final EventRepository eventRepository;
    private final RedisCacheService redisCacheService;
    private final ChallengeRepository challengeRepository;

    @Transactional
    public void getAllEvents() {
        List<TourApiItemDto> items = tourApiService.fetchAllEvents();

        List<String> contentIds = items.stream()
                .map(TourApiItemDto::getContentid)
                .toList();

        List<String> existingIds = eventRepository.findAllContentIdIn(contentIds);

        List<Event> newEvents = items.stream()
                .filter(dto -> !existingIds.contains(dto.getContentid()))
                .map(Event::fromEntity)
                .toList();

        eventRepository.saveAll(newEvents);
        log.info(" 총 {}개 이벤트 저장 완료", newEvents.size());
    }

    @Transactional
    public void updateTodayEvents(){
        List<TourApiItemDto> items = tourApiService.fetchTodayEvents();

        List<Integer> contentIds = items.stream()
                .map(TourApiItemDto::getContentid)
                .filter(id -> id != null && !id.isBlank())
                .map(Integer::parseInt)
                .toList();

        List<Event> existingEvents = eventRepository.findEventsByContentIds(contentIds);
        Map<Integer, Event> existingEventMap = new HashMap<>();
        for (Event event : existingEvents) {
            existingEventMap.put(event.getContentId(), event);
        }

        List<Event> toSave = new ArrayList<>();
        int updateCount = 0;
        int insertCount = 0;

        for (TourApiItemDto dto : items) {
            int contentId = Integer.parseInt(dto.getContentid());

            Event newEvent = Event.fromEntity(dto);

            if (existingEventMap.containsKey(contentId)) {
                Event existing = existingEventMap.get(contentId);
                existing.updateFrom(newEvent);
                toSave.add(existing);
                updateCount++;
            } else {
                toSave.add(newEvent);
                insertCount++;
            }
        }

        eventRepository.saveAll(toSave);

        // 축제 변경 시 캐시 무효화
        redisCacheService.deleteCache(RedisCacheService.UPCOMING_EVENTS_KEY);
        redisCacheService.deleteCache(RedisCacheService.ENDING_SOON_EVENTS_KEY);

        log.info("오늘 이벤트 upsert 완료: 총 {}, 업데이트 {}, 신규 {}", toSave.size(), updateCount, insertCount);
    }

    @Transactional
    public void updateEventDetails() {
        List<Event> events = eventRepository.findByEventDetailStatusOrderByStartDateDesc(EventDetailStatus.PENDING);
        int dailyLimit = 450;
        int processedEvents = 0; // 이벤트 수로 집계
        boolean hasStatusChanges = false;

        log.info("축제 상세정보 업데이트 시작");

        for (Event event : events) {
            EventDetailStatus currentStatus;
            if (processedEvents >= dailyLimit) {
                log.info("오늘의 업데이트 한도({}개)에 도달했습니다. 남은 이벤트: {}개", dailyLimit, events.size() - processedEvents);
                break;
            }

            if(event.getEventDetailStatus() == EventDetailStatus.PENDING) {
                CompletableFuture<TourApiDetailIntroDto> introFuture = null;
                CompletableFuture<List<TourApiDetailInfoDto>> infoFuture = null;

                try {
                    // 두 API를 비동기 호출
                    introFuture = tourApiService.fetchEventDetailIntro(event.getContentId());
                    infoFuture = tourApiService.fetchEventDetailInfo(event.getContentId());

                    // 타임아웃 설정 (30초)
                    CompletableFuture.allOf(introFuture, infoFuture).get(30, TimeUnit.SECONDS);
                    processedEvents++; // 이벤트 단위로 카운트

                    // 결과 가져오기
                    TourApiDetailIntroDto detailIntroDto = introFuture.get();
                    List<TourApiDetailInfoDto> detailInfoList = infoFuture.get();

                    // API에서 삭제된 경우
                    if (detailIntroDto == null && (detailInfoList == null || detailInfoList.isEmpty())) {
                        currentStatus = EventDetailStatus.DELETED_FROM_API;
                        log.warn("API에서 삭제된 이벤트 발견. contentId: {}", event.getContentId());
                    } else {
                        event.updateDetailFrom(detailIntroDto);
                        event.updateDetailInfoFrom(detailInfoList);
                        currentStatus = EventDetailStatus.SUCCESS;
                    }
                    hasStatusChanges = true;
                } catch (TimeoutException e) {
                    log.error("API 호출 타임아웃: contentId={}", event.getContentId(), e);

                    // 진행 중인 future 취소
                    if (introFuture != null) {
                        introFuture.cancel(true);
                    }
                    if (infoFuture != null) {
                        infoFuture.cancel(true);
                    }
                    processedEvents++;
                    continue;
                } catch (Exception e) {
                    log.error("축제 상세정보 업데이트 실패. contentId: {}, error: {}", event.getContentId(), e.getMessage());
                    currentStatus = EventDetailStatus.FAILED;
                    processedEvents++;
                    hasStatusChanges = true;
                }

                //결정된 상태를 엔티티에 반영하고 저장
                event.changeEventDetailStatus(currentStatus);
                eventRepository.save(event);
            }
        }

        // 상태 변경이 있었으면 관련 캐시 무효화
        if (hasStatusChanges) {
            redisCacheService.deleteCache(RedisCacheService.UPCOMING_EVENTS_KEY);
            redisCacheService.deleteCache(RedisCacheService.ENDING_SOON_EVENTS_KEY);
            redisCacheService.deleteCache(RedisCacheService.POPULAR_EVENTS_KEY);
        }

        log.info("전체 축제 상세정보 업데이트 완료");
    }


    public List<Event> getUpcomingEvents(){
        // 캐시에서 먼저 조회
        Optional<List<Event>> cachedEvents = redisCacheService.getCacheList(
                RedisCacheService.UPCOMING_EVENTS_KEY,
                new TypeReference<>() {
                }
        );

        if (cachedEvents.isPresent()) {
            return cachedEvents.get();
        }

        // 캐시에 없으면 DB에서 조회
        Pageable limit = PageRequest.of(0, 4);
        List<Event> events = eventRepository.findUpcomingEvents(limit);

        // 캐시에 저장
        redisCacheService.setCache(
                RedisCacheService.UPCOMING_EVENTS_KEY,
                events,
                RedisCacheService.UPCOMING_EVENTS_TTL
        );

        return events;
    }

    public List<Event> getEndingSoonEvents() {
        // 캐시에서 먼저 조회
        Optional<List<Event>> cachedEvents = redisCacheService.getCacheList(
                RedisCacheService.ENDING_SOON_EVENTS_KEY,
                new TypeReference<>() {
                }
        );

        if (cachedEvents.isPresent()) {
            return cachedEvents.get();
        }

        // 캐시에 없으면 DB에서 조회
        Pageable limit = PageRequest.of(0, 4);
        List<Event> events = eventRepository.findEndingSoonEvents(LocalDate.now(), limit);

        // 캐시에 저장
        redisCacheService.setCache(
                RedisCacheService.ENDING_SOON_EVENTS_KEY,
                events,
                RedisCacheService.ENDING_SOON_EVENTS_TTL
        );

        return events;
    }

    public List<Event> getPopularEvents(){
        // 캐시에서 먼저 조회
        Optional<List<Event>> cachedEvents = redisCacheService.getCacheList(
                RedisCacheService.POPULAR_EVENTS_KEY,
                new TypeReference<>() {
                }
        );

        if (cachedEvents.isPresent()) {
            return cachedEvents.get();
        }

        // 캐시에 없으면 DB에서 조회
        List<Event> events = eventRepository.findPopularEvents(4);

        // 캐시에 저장
        redisCacheService.setCache(
                RedisCacheService.POPULAR_EVENTS_KEY,
                events,
                RedisCacheService.POPULAR_EVENTS_TTL
        );

        return events;
    }

    public Page<EventResponseDto> getFilteredEvents(EventItemFilterRequestDto request, Pageable pageable) {
        return eventRepository.findFiltered(request, pageable)
                .map(EventResponseDto::from);
    }

    @Transactional
    public EventDetailResponseDto getEventDetail(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(EventNotFoundException::new);

        // 관련 도전과제 Id (값이 없으면 null)
        Long relatedChallengeId = challengeRepository.findFirstByEvent(event).map(Challenge::getId).orElse(null);

        //DB에 상세정보가 없으면 tourAPI 호출
        if(event.getEventDetailStatus() == EventDetailStatus.PENDING){
            EventDetailStatus currentStatus;

            try{
                // 두 API를 비동기 호출
                CompletableFuture<TourApiDetailIntroDto> introFuture = tourApiService.fetchEventDetailIntro(event.getContentId());
                CompletableFuture<List<TourApiDetailInfoDto>> infoFuture = tourApiService.fetchEventDetailInfo(event.getContentId());

                // 타임아웃 설정 (30초)
                CompletableFuture.allOf(
                        introFuture.orTimeout(30, TimeUnit.SECONDS),
                        infoFuture.orTimeout(30, TimeUnit.SECONDS)
                ).join();

                // 결과 가져오기
                TourApiDetailIntroDto detailIntroDto = introFuture.get();
                List<TourApiDetailInfoDto> detailInfoList = infoFuture.get();

                // API에서 삭제된 경우
                if (detailIntroDto == null && (detailInfoList == null || detailInfoList.isEmpty())) {
                    currentStatus = EventDetailStatus.DELETED_FROM_API;
                    log.warn("API에서 삭제된 이벤트 발견. contentId: {}", event.getContentId());
                } else {
                    currentStatus = EventDetailStatus.SUCCESS;
                    event.updateDetailFrom(detailIntroDto);
                    event.updateDetailInfoFrom(detailInfoList);
                }
            } catch (Exception e){
                log.error("축제 상세 정보 업데이트 실패: eventId={}, error={}", eventId, e.getMessage());
                currentStatus = EventDetailStatus.FAILED;
            }

            event.changeEventDetailStatus(currentStatus);
            eventRepository.save(event);
        }

        return EventDetailResponseDto.from(event, relatedChallengeId);
    }
}