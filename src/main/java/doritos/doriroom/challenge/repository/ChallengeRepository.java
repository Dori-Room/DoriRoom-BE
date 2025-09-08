package doritos.doriroom.challenge.repository;

import doritos.doriroom.challenge.domain.challenge.Challenge;
import doritos.doriroom.challenge.domain.challenge.ChallengeGroup;
import doritos.doriroom.challenge.domain.challenge.ChallengeType;
import doritos.doriroom.event.domain.Event;
import doritos.doriroom.tourApi.domain.AreaGroup;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

//import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    @NonNull
    Optional<Challenge> findById(@NonNull Long challengeId); // 단일과제 조회
    Optional<Challenge> findFirstByEvent(Event event); // 해당 이벤트와 관련된 도전과제  (첫 번째 하나만 조회)

    List<Challenge> findByChallengeType(ChallengeType challengeType); // 도전과제 타입별 리스트 조회

    // 지역-일반과제로 조회 (일반과제 리스트 조회 시 사용) (reward 포함)
    @Query("SELECT DISTINCT c FROM Challenge c LEFT JOIN FETCH c.rewards WHERE c.challengeGroup = :challengeGroup")
    List<Challenge> findByChallengeGroupWithRewards(@Param("challengeGroup") ChallengeGroup challengeGroup);

    // 특정 지역별 리스트 조회 (reward 포함)
    @Query("SELECT DISTINCT c FROM Challenge c LEFT JOIN FETCH c.rewards WHERE c.challengeGroup = :challengeGroup AND c.areaGroup = :areaGroup")
    List<Challenge> findByChallengeGroupAndAreaGroupWithRewards(@Param("challengeGroup") ChallengeGroup challengeGroup, @Param("areaGroup") AreaGroup areaGroup);


//    // 현재 진행 중인 도전과제 조회 (날짜 범위 내)
//    @Query("SELECT c FROM Challenge c WHERE (c.startDate IS NULL OR c.startDate <= :currentDate) " +
//            "AND (c.endDate IS NULL OR c.endDate >= :currentDate)")
//    List<Challenge> findActiveChallenges(@Param("currentDate") LocalDate currentDate);
//
//    // 그룹별 현재 진행 중인 도전과제 조회
//    @Query("SELECT c FROM Challenge c WHERE c.challengeGroup = :challengeGroup " +
//            "AND c.startDate <= :currentDate AND c.endDate >= :currentDate")
//    List<Challenge> findActiveChallengesByGroup(@Param("challengeGroup") ChallengeGroup challengeGroup,
//                                                @Param("currentDate") LocalDate currentDate);
//
//    // 지역별 현재 진행 중인 도전과제 조회
//    @Query("SELECT c FROM Challenge c WHERE c.challengeGroup = :challengeGroup " +
//            "AND c.areaGroup = :areaGroup " +
//            "AND c.startDate <= :currentDate AND c.endDate >= :currentDate")
//    List<Challenge> findActiveChallengesByGroupAndArea(@Param("challengeGroup") ChallengeGroup challengeGroup,
//                                                       @Param("areaGroup") AreaGroup areaGroup,
//                                                       @Param("currentDate") LocalDate currentDate);
}
