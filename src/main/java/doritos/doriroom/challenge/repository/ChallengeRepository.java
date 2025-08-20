package doritos.doriroom.challenge.repository;

import doritos.doriroom.challenge.domain.challenge.Challenge;
import doritos.doriroom.challenge.domain.challenge.ChallengeGroup;
import doritos.doriroom.tourApi.domain.AreaGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {
    Optional<Challenge> findById(Long challengeId); // 단일과제 조회
    List<Challenge> findByChallengeGroup(ChallengeGroup challengeGroup); // 지역-일반과제로 조회 (일반과제 리스트 조회 시 사용)
    List<Challenge> findByChallengeGroupAndAreaGroup(ChallengeGroup challengeGroup, AreaGroup areaGroup); // 특정 지역별 리스트 조회
}
