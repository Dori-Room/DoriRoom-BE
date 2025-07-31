package doritos.doriroom.event.service;

import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.dto.request.EventItemFilterRequestDto;
import doritos.doriroom.event.dto.response.EventDetailResponseDto;
import doritos.doriroom.event.dto.response.EventResponseDto;
import doritos.doriroom.event.exception.EventNotFoundException;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.tourApi.dto.response.TourApiDetailInfoDto;
import doritos.doriroom.tourApi.dto.response.TourApiDetailIntroDto;
import doritos.doriroom.tourApi.dto.response.TourApiItemDto;
import doritos.doriroom.tourApi.service.TourApiService;
import doritos.doriroom.event.repository.EventRepository;
import java.time.LocalDate;
import java.util.*;
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
        List<Event> events = eventRepository.findEventsNeedingDetailUpdate();
        int dailyLimit = 900;
        int processedCount = 0;

        log.info("축제 상세정보 업데이트 시작");

        for (Event event : events) {
            if (processedCount >= dailyLimit) {
                log.info("오늘의 업데이트 한도({}개)에 도달했습니다. 남은 이벤트: {}개", dailyLimit, events.size() - processedCount);
                break;
            }

            try {
                // detailIntro2 업데이트
                TourApiDetailIntroDto detailIntroDto = tourApiService.fetchEventDetailIntro(event.getContentId());
                event.updateDetailFrom(detailIntroDto);
                processedCount++;

                // detailInfo2 업데이트
                List<TourApiDetailInfoDto> detailInfoList = tourApiService.fetchEventDetailInfo(event.getContentId());
                event.updateDetailInfoFrom(detailInfoList);
                processedCount++;

                eventRepository.save(event);

                // API 호출 간격 조절
                Thread.sleep(500);

            } catch (Exception e) {
                log.error("축제 상세정보 업데이트 실패. contentId: {}, error: {}", event.getContentId(), e.getMessage());
                processedCount += 2;
            }
        }
        log.info("전체 축제 상세정보 업데이트 완료");
    }


    public List<Event> getUpcomingEvents(){
        Pageable limit = PageRequest.of(0, 4);
        return eventRepository.findUpcomingEvents(limit);
    }

    public List<Event> getEndingSoonEvents() {
        Pageable limit = PageRequest.of(0, 4);
        return eventRepository.findEndingSoonEvents(LocalDate.now(), limit);
    }

    public Page<EventResponseDto> getFilteredEvents(EventItemFilterRequestDto request, Pageable pageable) {
        return eventRepository.findFiltered(request, pageable)
            .map(EventResponseDto::from);
    }

    public EventDetailResponseDto getEventDetail(UUID contentId) {
        Event event = eventRepository.findById(contentId)
            .orElseThrow(EventNotFoundException::new);
        return EventDetailResponseDto.from(event);
    }

    //도별 축제 정보 반환
    public Page<EventResponseDto> getEventsByAreaGroup(AreaGroup areaGroup, Pageable pageable) {
        List<Integer> areaCodes = areaGroup.getAreaCodes();

        Page<Event> events = eventRepository.findByAreaCodesIn(areaCodes, pageable);
        return events.map(EventResponseDto::from);
    }
}
