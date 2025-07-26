package doritos.doriroom.event.service;

import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.dto.response.EventResponseDto;
import doritos.doriroom.tourApi.domain.AreaGroup;
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

        // 기존 데이터 Map으로 만들기
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
                toSave.add(newEvent); // 신규 insert
                insertCount++;
            }
        }

        eventRepository.saveAll(toSave);
        log.info("오늘 이벤트 upsert 완료: 총 {}, 업데이트 {}, 신규 {}", toSave.size(), updateCount, insertCount);
    }

    public List<Event> getUpcomingEvents(){
        Pageable limit = PageRequest.of(0, 4);

        return eventRepository.findUpcomingEvents(limit);
    }

    public List<Event> getEndingSoonEvents(){
        Pageable limit = PageRequest.of(0, 4);
        return  eventRepository.findEndingSoonEvents(LocalDate.now(), limit);
    }

    public Page<EventResponseDto> getEventsByAreaGroup(Integer groupCode, Pageable pageable) {
        AreaGroup areaGroup = AreaGroup.fromCode(groupCode);
        return eventRepository.findByAreaCodeIn(areaGroup.getAreaCodes(), pageable)
            .map(EventResponseDto::from);
    }

    public Page<EventResponseDto> getEventsByAreaAndSigungu(Integer areaCode, List<Integer> sigunguCodes, Pageable pageable) {
        return eventRepository.findByAreaCodeAndSigunguCodes(areaCode, sigunguCodes, pageable)
            .map(EventResponseDto::from);
    }

    public Page<EventResponseDto> getEventsByCategoryCode(String categoryCode, Pageable pageable) {
        return eventRepository.findByCategoryCode(categoryCode, pageable)
            .map(EventResponseDto::from);
    }
}
