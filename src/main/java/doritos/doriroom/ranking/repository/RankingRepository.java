package doritos.doriroom.ranking.repository;

import doritos.doriroom.atlas.domain.UserAtlas;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import java.util.Optional;
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

    // 특정 유저의 랭킹 조회
    @Query("SELECT COUNT(u) + 1 " +
           "FROM User u " +
           "WHERE u.likeCount > (SELECT u2.likeCount FROM User u2 WHERE u2.userId = :userId)")
    Integer findMyDenseRankByUserId(@Param("userId") UUID userId);

    // 사용자의 좋아요 점수가 0이거나 없을 경우를 대비해 Optional로 처리
    @Query("SELECT u.likeCount FROM User u WHERE u.userId = :userId")
    Optional<Long> findLikeCountByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT COUNT(ua) + 1
        FROM UserAtlas ua
        WHERE ua.atlas.areaGroup = :areaGroup
          AND (ua.level > :myLevel OR (ua.level = :myLevel AND ua.currentExp > :myExp))
        """)
    Long findMyRankByScore(@Param("areaGroup") AreaGroup areaGroup,
        @Param("myLevel") Integer myLevel,
        @Param("myExp") Long myExp);

    // 특정 유저의 지역별 도감 정보 조회
    @Query("""
        SELECT ua FROM UserAtlas ua
        JOIN ua.user u
        JOIN ua.atlas a
        WHERE u.userId = :userId AND a.areaGroup = :areaGroup
        """)
    Optional<UserAtlas> findUserAtlasByUserIdAndAreaGroup(@Param("userId") UUID userId, @Param("areaGroup") AreaGroup areaGroup);

    // 닉네임으로 유저 검색
    @Query("SELECT u FROM User u WHERE u.nickname LIKE %:nickname% ORDER BY u.nickname")
    List<User> findByNicknameContainingOrderByLikeCountDesc(@Param("nickname") String nickname);

    // 내가 팔로우하는 유저 내에서 닉네임으로 유저 검색
    @Query("""
        SELECT f.followed FROM Follow f
        JOIN f.followed u
        WHERE f.follower.userId = :userId
        AND u.nickname LIKE %:nickname%
        ORDER BY u.nickname
        """)
    List<User> findFollowingUsersByNicknameContaining(@Param("userId") UUID userId, @Param("nickname") String nickname);
} 