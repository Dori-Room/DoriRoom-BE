package doritos.doriroom.atlas.repository;

import doritos.doriroom.atlas.domain.Atlas;
import doritos.doriroom.atlas.domain.AtlasReward;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AtlasRewardRepository extends JpaRepository<AtlasReward, Long> {
    List<AtlasReward> findByAtlasAndTargetLevel(Atlas atlas, int level); // 해당 지역도감의 보상아이템들 조회 시 (현재는 1개만)

    // 유저의 현재 레벨 기준으로 받을 수 있는 바로 다음의 보상 아이템 조회
    Optional<AtlasReward> findFirstByAtlasAndTargetLevelGreaterThanOrderByTargetLevelAsc(Atlas atlas, int currentLevel);

    // 특정 지역의 모든 보상 아이템을 레벨 순으로 조회
    List<AtlasReward> findByAtlasOrderByTargetLevel(Atlas atlas);

    // 특정 지역의 특정 레벨 이하 보상 아이템들 조회
    List<AtlasReward> findByAtlasAndTargetLevelLessThanEqualOrderByTargetLevel(Atlas atlas, int level);
}
