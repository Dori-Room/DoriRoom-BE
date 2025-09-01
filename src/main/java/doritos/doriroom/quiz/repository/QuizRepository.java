package doritos.doriroom.quiz.repository;

import doritos.doriroom.quiz.domain.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    // 도전과제 id로 퀴즈를 조회, 관련 문제들 목록을 조회하도록 fetch join 사용
    @Query("SELECT DISTINCT q FROM Quiz q JOIN FETCH q.questions WHERE q.challenge.id = :challengeId")
    Optional<Quiz> findWithQuestionsByChallengeId(@Param("challengeId") Long challengeId);

}