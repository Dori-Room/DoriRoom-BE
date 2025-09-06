package doritos.doriroom.diary.repository;

import doritos.doriroom.diary.domain.Diary;
import doritos.doriroom.user.domain.RoomVisibility;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Query("""
        SELECT d FROM Diary d
        WHERE d.userId = :userId
        AND d.diaryVisibility = 'PUBLIC'
        AND d.visitedAt BETWEEN :startDate AND :endDate
        ORDER BY d.visitedAt
        """)
    List<Diary> findPublicByUserIdAndVisitedAtBetweenOrderByVisitedAt(
        @Param("userId") UUID userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    boolean existsByUserIdAndEventId(UUID userId, UUID eventId);

    Page<Diary> findByUserIdOrderByVisitedAtDesc(UUID userId, Pageable pageable);

    @Query("""
        SELECT d FROM Diary d
        WHERE d.userId = :userId
        AND d.diaryVisibility IN (:visibilities)
        AND d.visitedAt BETWEEN :startDate AND :endDate
        ORDER BY d.visitedAt
        """)
    List<Diary> findPublicAndFollowersByUserIdAndVisitedAtBetweenOrderByVisitedAt(
        @Param("userId") UUID userId,
        @Param("visibilities") List<RoomVisibility> visibilities,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT d FROM Diary d
        WHERE d.diaryVisibility = 'PUBLIC'
        AND d.createdAt >= :startDate
        AND d.createdAt <= :endDate
        ORDER BY (d.likes * 2) DESC, d.createdAt DESC
        limit 5
        """)
    List<Diary> findPopularDiariesByMonth(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable);

    @Query("""
    SELECT d FROM Diary d
    WHERE d.createdAt >= :twoWeeksAgo AND (
        (d.userId IN :followingIds AND d.diaryVisibility = 'PUBLIC') OR
        (d.userId IN :mutualBestFriendIds AND d.diaryVisibility = 'FOLLOWERS')
    )
    ORDER BY d.createdAt DESC, d.diaryId DESC
    """)
    Page<Diary> findFriendsDiaries(
        @Param("followingIds") List<UUID> followingIds,
        @Param("mutualBestFriendIds") List<UUID> mutualBestFriendIds,
        @Param("twoWeeksAgo") LocalDateTime twoWeeksAgo,
        Pageable pageable);

    @Query("""
        SELECT d FROM Diary d
        WHERE d.userId = :userId
        AND d.diaryVisibility = 'PUBLIC'
        AND d.visitedAt = :visitedAt
        ORDER BY d.createdAt DESC
        """)
    List<Diary> findPublicByUserIdAndVisitedAtOrderByCreatedAtDesc(
        @Param("userId") UUID userId,
        @Param("visitedAt") LocalDate visitedAt);

    @Query("""
        SELECT d FROM Diary d
        WHERE d.userId = :userId
        AND d.diaryVisibility IN (:visibilities)
        AND d.visitedAt = :visitedAt
        ORDER BY d.createdAt DESC
        """)
    List<Diary> findPublicAndFollowersByUserIdAndVisitedAtOrderByCreatedAtDesc(
        @Param("userId") UUID userId,
        @Param("visibilities") List<RoomVisibility> visibilities,
        @Param("visitedAt") LocalDate visitedAt);
}
