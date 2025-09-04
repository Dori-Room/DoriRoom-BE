package doritos.doriroom.diary.repository;

import doritos.doriroom.diary.domain.DiaryLike;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DiaryLikeRepository extends JpaRepository<DiaryLike, UUID> {
    @Query("SELECT dl FROM DiaryLike dl WHERE dl.user.userId = :userId AND dl.diary.diaryId = :diaryId")
    Optional<DiaryLike> findByUserIdAndDiaryId(@Param("userId") UUID userId, @Param("diaryId") UUID diaryId);

    @Modifying
    @Transactional
    @Query("DELETE FROM DiaryLike dl WHERE dl.diary.diaryId = :diaryId")
    void deleteByDiaryId(@Param("diaryId") UUID diaryId);
}
