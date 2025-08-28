package doritos.doriroom.atlas.repository;

import doritos.doriroom.atlas.domain.Atlas;
import doritos.doriroom.atlas.domain.AtlasReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AtlasRewardRepository extends JpaRepository<AtlasReward, Long> {
    List<AtlasReward> findByAtlasAndTargetLevel(Atlas atlas, int level); // 해당 지역도감의 보상아이템들 조회 시 (현재는 1개만)

    // 모든 atlas에 대한 모든 보상 아이템을 한 번에 조회
    @Query("SELECT ar FROM AtlasReward ar WHERE ar.atlas.id IN :atlasIds ORDER BY ar.atlas.id, ar.targetLevel")
    List<AtlasReward> findByAtlasIdInOrderByTargetLevel(@Param("atlasIds") List<Long> atlasIds);

}
