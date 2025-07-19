package doritos.doriroom.event.repository;

import doritos.doriroom.event.domain.Event;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("SELECT e.contentId FROM Event e WHERE e.contentId IN :contentIds")
    List<String> findAllContentIdIn(@Param("contentIds") List<String> contentIds);

    @Query("SELECT e FROM Event e WHERE e.contentId IN :contentIds")
    List<Event> findEventsByContentIds(@Param("contentIds") List<String> contentIds);
}
