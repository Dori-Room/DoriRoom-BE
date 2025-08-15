package doritos.doriroom.diary.repository;

import doritos.doriroom.diary.domain.Diary;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, UUID> {
    List<Diary> findByUserIdAndVisitedAtBetweenOrderByVisitedAt(
        UUID userId, LocalDate startDate, LocalDate endDate);

    List<Diary> findByUserIdAndVisitedAtOrderByCreatedAtDesc(UUID userId, LocalDate visitedAt);

    @Query("SELECT d FROM Diary d WHERE d.eventId = :eventId AND d.diaryVisibility = 'PUBLIC' ORDER BY d.createdAt DESC")
    Page<Diary> findPublicDiariesByEventId(@Param("eventId") UUID eventId, Pageable pageable);

    @Query("SELECT d FROM Diary d WHERE d.userId = :userId AND d.diaryVisibility = 'PUBLIC' ORDER BY d.visitedAt DESC, d.createdAt DESC")
    Page<Diary> findPublicByUserIdOrderByVisitedAtDesc(@Param("userId") UUID userId, Pageable pageable);
}
