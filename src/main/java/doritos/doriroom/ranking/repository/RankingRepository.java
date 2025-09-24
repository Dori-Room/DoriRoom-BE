package doritos.doriroom.ranking.repository;

import doritos.doriroom.atlas.domain.UserAtlas;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RankingRepository extends JpaRepository<User, UUID> {
    
    // 좋아요 수 기준 상위 100명 조회
    List<User> findTop100ByOrderByLikeCountDesc();
    
    // 지역별 도감 레벨, 경험치 기준 상위 100명 조회
    @Query("""
        SELECT ua FROM UserAtlas ua
        JOIN ua.user u
        JOIN ua.atlas a
        WHERE a.areaGroup = :areaGroup
        ORDER BY ua.level DESC, ua.currentExp DESC
        """)
    List<UserAtlas> findTop100ByAreaGroupOrderByLevelDescAndExpDesc(@Param("areaGroup") AreaGroup areaGroup);
} 