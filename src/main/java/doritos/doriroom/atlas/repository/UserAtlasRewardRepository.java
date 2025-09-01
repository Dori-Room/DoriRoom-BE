package doritos.doriroom.atlas.repository;

import doritos.doriroom.atlas.domain.UserAtlasReward;
import doritos.doriroom.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserAtlasRewardRepository extends JpaRepository<UserAtlasReward, UUID> {

//    List<UserAtlasReward> findByUser(User user); // 특정 유저가 받은 모든 보상 아이템 조회
    @Query("SELECT uar FROM UserAtlasReward uar JOIN FETCH uar.atlasReward ar JOIN FETCH ar.atlas WHERE uar.user = :user")
    List<UserAtlasReward> findByUserWithDetails(@Param("user") User user); // fetch join 적용 AtlasReward와 Atlas을 함께 조회

    // 특정 유저가 특정 지역에서 받은 보상 아이템들 조회
    @Query("SELECT uar FROM UserAtlasReward uar " +
            "WHERE uar.user = :user AND uar.atlasReward.atlas.id = :atlasId")
    List<UserAtlasReward> findByUserAndAtlasId(@Param("user") User user, @Param("atlasId") Long atlasId);

    // 특정 유저가 특정 보상을 이미 받았는지 확인
    boolean existsByUserAndAtlasReward_Id(User user, Long atlasRewardId);
}