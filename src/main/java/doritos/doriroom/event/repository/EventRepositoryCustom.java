package doritos.doriroom.event.repository;

import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.dto.request.EventItemFilterRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventRepositoryCustom {
    Page<Event> findFiltered(EventItemFilterRequestDto filter, Pageable pageable);
}
