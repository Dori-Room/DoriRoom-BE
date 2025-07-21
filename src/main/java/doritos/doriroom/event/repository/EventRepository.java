package doritos.doriroom.event.repository;

import doritos.doriroom.event.domain.Event;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, UUID> {
    @Query("SELECT e.contentId FROM Event e WHERE e.contentId IN :contentIds")
    List<String> findAllContentIdIn(@Param("contentIds") List<String> contentIds);

    @Query("SELECT e FROM Event e WHERE e.contentId IN :contentIds")
    List<Event> findEventsByContentIds(@Param("contentIds") List<String> contentIds);

    @Query("""
    SELECT e FROM Event e
    WHERE e.startDate = CURRENT_DATE
    ORDER BY e.startDate ASC
    """)
    List<Event> findUpcomingEvents(Pageable pageable);

    @Query("SELECT e FROM Event e " +
        "WHERE e.startDate <= :today AND e.endDate >= :today " +
        "ORDER BY e.endDate ASC")
    List<Event> findEndingSoonEvents(@Param("today") LocalDate today, Pageable pageable);
}
