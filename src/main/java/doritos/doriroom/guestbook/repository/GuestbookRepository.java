package doritos.doriroom.guestbook.repository;

import doritos.doriroom.guestbook.domain.Guestbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GuestbookRepository extends JpaRepository<Guestbook, UUID> {
    
    @Query("SELECT g FROM Guestbook g WHERE g.roomOwnerId = :roomOwnerId ORDER BY g.createdAt DESC")
    Page<Guestbook> findByRoomOwnerIdOrderByCreatedAtDesc(@Param("roomOwnerId") UUID roomOwnerId, Pageable pageable);
}