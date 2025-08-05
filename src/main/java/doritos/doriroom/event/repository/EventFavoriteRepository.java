package doritos.doriroom.event.repository;

import doritos.doriroom.event.domain.EventFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface EventFavoriteRepository extends JpaRepository<EventFavorite, UUID> {

    @Query("SELECT ef FROM EventFavorite ef WHERE ef.user.id = :userId AND ef.event.eventId = :eventId")
    Optional<EventFavorite> findByUserIdAndEventId(@Param("userId") UUID userId, @Param("eventId") UUID eventId);

    @Query("SELECT ef FROM EventFavorite ef JOIN FETCH ef.event WHERE ef.user.id = :userId ORDER BY ef.createdAt DESC")
    Page<EventFavorite> findFavoritesWithEventByUserId(@Param("userId") UUID userId, Pageable pageable);
}