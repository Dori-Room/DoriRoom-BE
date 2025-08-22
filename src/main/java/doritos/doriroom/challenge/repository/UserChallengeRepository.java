package doritos.doriroom.challenge.repository;

import doritos.doriroom.challenge.domain.challenge.Challenge;
import doritos.doriroom.challenge.domain.userchallenge.ChallengeStatus;
import doritos.doriroom.challenge.domain.userchallenge.UserChallenge;
import doritos.doriroom.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserChallengeRepository extends JpaRepository<UserChallenge, Long> {
    Optional<UserChallenge> findByUserAndChallenge(User user, Challenge challenge); // 유저의 도전과제 상태 조회 시
    List<UserChallenge> findByUserAndChallengeIn(User user, List<Challenge> challenges); // 유저의 도전과제들 전체 상태 조회 시


    List<UserChallenge> findByUserAndStatus(User user, ChallengeStatus status); // 특정 유저의 도전과제 상태별 조회
    List<UserChallenge> findByUser(User user);     // 특정 유저의 모든 도전과제 조회


//    // 특정 도전과제에 참여 중인 사용자 수 조회
//    @Query("SELECT COUNT(uc) FROM UserChallenge uc WHERE uc.challenge = :challenge AND uc.status != 'COMPLETED'")
//    long countParticipatingUsers(@Param("challenge") Challenge challenge);
//
//    // 특정 도전과제를 완료한 사용자 수 조회
//    @Query("SELECT COUNT(uc) FROM UserChallenge uc WHERE uc.challenge = :challenge AND uc.status = 'COMPLETED'")
//    long countCompletedUsers(@Param("challenge") Challenge challenge);
}
