package doritos.doriroom.event.service;

import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.dto.response.EventApiItemDto;
import doritos.doriroom.event.repository.EventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class EventService {
    private final tourApiService tourApiService;
    private final EventRepository eventRepository;

    @Transactional
    public void getAllEvents() {
        List<EventApiItemDto> items = tourApiService.fetchEvents();

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
}
