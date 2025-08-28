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

    // 유저의 도전과제들 전체 상태 조회 시
    @Query("SELECT uc FROM UserChallenge uc JOIN FETCH uc.challenge c WHERE uc.user = :user AND c IN :challenges")
    List<UserChallenge> findByUserAndChallengeInWithFetch(@Param("user") User user, @Param("challenges") List<Challenge> challenges);


    List<UserChallenge> findByUserAndStatus(User user, ChallengeStatus status); // 특정 유저의 도전과제 상태별 조회
    List<UserChallenge> findByUser(User user);     // 특정 유저의 모든 도전과제 조회


//    // 보상 정보까지 조회
//    @Query("SELECT uc FROM UserChallenge uc JOIN FETCH uc.challenge c LEFT JOIN FETCH c.rewards WHERE uc.user = :user AND c IN :challenges")
//    List<UserChallenge> findByUserAndChallengeInWithAllRelations(@Param("user") User user, @Param("challenges") List<Challenge> challenges);

}

