package doritos.doriroom.event.service;

import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.dto.response.EventApiItemDto;
import doritos.doriroom.event.repository.EventRepository;
import java.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        List<EventApiItemDto> items = tourApiService.fetchAllEvents();

        List<String> contentIds = items.stream()
            .map(EventApiItemDto::getContentid)
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
        List<EventApiItemDto> items = tourApiService.fetchTodayEvents();

        List<String> contentIds = new ArrayList<>();
        for(EventApiItemDto item : items){
            if(item.getContentid() != null && !item.getContentid().isBlank()){
                contentIds.add(item.getContentid());
            }
        }

        // 기존 데이터 Map으로 만들기
        List<Event> existingEvents = eventRepository.findEventsByContentIds(contentIds);
        Map<String, Event> existingEventMap = new HashMap<>();
        for (Event event : existingEvents) {
            existingEventMap.put(event.getContentId(), event);
        }

        List<Event> toSave = new ArrayList<>();
        int updateCount = 0;
        int insertCount = 0;

        for (EventApiItemDto dto : items) {
            String contentId = dto.getContentid();
            if (contentId == null || contentId.isBlank()) continue;

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
}
