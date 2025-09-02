package doritos.doriroom.event.service;

import com.fasterxml.jackson.core.type.TypeReference;
import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.domain.EventDetailStatus;
import doritos.doriroom.event.dto.request.EventItemFilterRequestDto;
import doritos.doriroom.event.dto.response.EventDetailResponseDto;
import doritos.doriroom.event.dto.response.EventResponseDto;
import doritos.doriroom.event.exception.EventNotFoundException;
import doritos.doriroom.global.cache.RedisCacheService;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.tourApi.dto.response.TourApiDetailInfoDto;
import doritos.doriroom.tourApi.dto.response.TourApiDetailIntroDto;
import doritos.doriroom.tourApi.dto.response.TourApiItemDto;
import doritos.doriroom.tourApi.service.TourApiService;
import doritos.doriroom.event.repository.EventRepository;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
        log.info("오늘 이벤트 upsert 완료: 총 {}, 업데이트 {}, 신규 {}", toSave.size(), updateCount, insertCount);
    }

    @Transactional
    public void updateEventDetails() {
        List<Event> events = eventRepository.findByEventDetailStatusOrderByStartDateDesc(EventDetailStatus.PENDING);
        int dailyLimit = 900;
        int processedCount = 0;

        log.info("축제 상세정보 업데이트 시작");

        for (Event event : events) {
            EventDetailStatus currentStatus;
            if (processedCount >= dailyLimit) {
                log.info("오늘의 업데이트 한도({}개)에 도달했습니다. 남은 이벤트: {}개", dailyLimit, events.size() - processedCount);
                break;
            }

            if(event.getEventDetailStatus() == EventDetailStatus.PENDING) {
                try {
                    // 두 API를 비동기 호출
                    CompletableFuture<TourApiDetailIntroDto> introFuture = tourApiService.fetchEventDetailIntro(
                        event.getContentId());
                    CompletableFuture<List<TourApiDetailInfoDto>> infoFuture = tourApiService.fetchEventDetailInfo(
                        event.getContentId());

                    // 두 작업이 모두 끝날 때까지 기다림
                    CompletableFuture.allOf(introFuture, infoFuture).join();
                    processedCount += 2;

                    // 결과 가져오기
                    TourApiDetailIntroDto detailIntroDto = introFuture.get();
                    List<TourApiDetailInfoDto> detailInfoList = infoFuture.get();

                    // API에서 삭제된 경우
                    if (detailIntroDto == null && (detailInfoList == null
                        || detailInfoList.isEmpty())) {
                        currentStatus = EventDetailStatus.DELETED_FROM_API;
                        log.warn("API에서 삭제된 이벤트 발견. contentId: {}", event.getContentId());
                    } else {
                        event.updateDetailFrom(detailIntroDto);
                        event.updateDetailInfoFrom(detailInfoList);
                        currentStatus = EventDetailStatus.SUCCESS;
                    }
                } catch (Exception e) {
                    log.error("축제 상세정보 업데이트 실패. contentId: {}, error: {}", event.getContentId(),
                        e.getMessage());
                    currentStatus = EventDetailStatus.FAILED;
                    processedCount += 2;
                }

                //결정된 상태를 엔티티에 반영하고 저장
                event.changeEventDetailStatus(currentStatus);
                eventRepository.save(event);
            }
        }
        log.info("전체 축제 상세정보 업데이트 완료");
    }


    public List<Event> getUpcomingEvents(){
        // 캐시에서 먼저 조회
        Optional<List<Event>> cachedEvents = redisCacheService.getCacheList(
            RedisCacheService.UPCOMING_EVENTS_KEY,
            new TypeReference<List<Event>>() {}
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
            new TypeReference<List<Event>>() {}
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
        return eventRepository.findPopularEvents(4);
    }

    public Page<EventResponseDto> getFilteredEvents(EventItemFilterRequestDto request, Pageable pageable) {
        return eventRepository.findFiltered(request, pageable)
            .map(EventResponseDto::from);
    }

    @Transactional
    public EventDetailResponseDto getEventDetail(UUID eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(EventNotFoundException::new);

        //DB에 상세정보가 없으면 tourAPI 호출
        if(event.getEventDetailStatus() == EventDetailStatus.PENDING){
            EventDetailStatus currentStatus;
            try{
                // 두 API를 비동기 호출
                CompletableFuture<TourApiDetailIntroDto> introFuture = tourApiService.fetchEventDetailIntro(event.getContentId());
                CompletableFuture<List<TourApiDetailInfoDto>> infoFuture = tourApiService.fetchEventDetailInfo(event.getContentId());

                // 두 작업이 모두 끝날 때까지 기다림
                CompletableFuture.allOf(introFuture, infoFuture).join();

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
                currentStatus = EventDetailStatus.FAILED;
                log.error("축제 상세 정보 업데이트 실패: eventId={}, error={}", eventId, e.getMessage());
            }
            event.changeEventDetailStatus(currentStatus);
            eventRepository.save(event);
        }

        return EventDetailResponseDto.from(event);
    }

    //도별 축제 정보 반환
    public Page<EventResponseDto> getEventsByAreaGroup(AreaGroup areaGroup, Pageable pageable) {
        List<Integer> areaCodes = areaGroup.getAreaCodes();

        Page<Event> events = eventRepository.findByAreaCodesIn(areaCodes, pageable);
        return events.map(EventResponseDto::from);
    }
}
