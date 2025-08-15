package doritos.doriroom.diary.repository;

import doritos.doriroom.diary.domain.Diary;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, UUID> {
    List<Diary> findByUserIdAndVisitedAtBetweenOrderByVisitedAt(
        UUID userId, LocalDate startDate, LocalDate endDate);

    List<Diary> findByUserIdAndVisitedAtOrderByCreatedAtDesc(UUID userId, LocalDate visitedAt);
}
