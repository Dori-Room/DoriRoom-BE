package doritos.doriroom.ranking.repository;

import doritos.doriroom.atlas.domain.UserAtlas;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import java.util.Optional;
import java.util.Set;
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
        AND u.isWithdraw = false
        ORDER BY ua.level DESC, ua.currentExp DESC
        """)
    List<UserAtlas> findTop100ByAreaGroupOrderByLevelDescAndExpDesc(@Param("areaGroup") AreaGroup areaGroup);

    // 특정 유저의 랭킹 조회
    @Query("SELECT COUNT(u) + 1 " +
           "FROM User u " +
           "WHERE u.likeCount > (SELECT u2.likeCount FROM User u2 WHERE u2.userId = :userId AND u.isWithdraw = false)")
    Integer findMyDenseRankByUserId(@Param("userId") UUID userId);

    // 사용자의 좋아요 점수가 0이거나 없을 경우를 대비해 Optional로 처리
    @Query("SELECT u.likeCount FROM User u WHERE u.userId = :userId")
    Optional<Long> findLikeCountByUserId(@Param("userId") UUID userId);

    @Query("""
        SELECT COUNT(ua) + 1
        FROM UserAtlas ua
        JOIN ua.user u
        WHERE ua.atlas.areaGroup = :areaGroup
          AND (ua.level > :myLevel OR (ua.level = :myLevel AND ua.currentExp > :myExp)
          AND u.isWithdraw = false)
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
    @Query("""
        SELECT u FROM User u
        WHERE u.nickname
        LIKE %:nickname%
        AND u.isWithdraw = false
        AND u.userId != :currentUserId
        ORDER BY u.nickname""")
    List<User> findByNicknameContainingOrderByLikeCountDesc(@Param("nickname") String nickname, @Param("currentUserId") UUID currentUserId);

    // 내가 팔로우하는 유저 내에서 닉네임으로 유저 검색
    @Query("""
        SELECT f.followed FROM Follow f
        JOIN f.followed u
        WHERE f.follower.userId = :userId
        AND u.nickname LIKE %:nickname%
        AND u.isWithdraw = false
        ORDER BY u.likeCount DESC, u.nickname
        """)
    List<User> findFollowingUsersByNicknameContaining(@Param("userId") UUID userId, @Param("nickname") String nickname);

    // 나를 팔로우하는 유저 중에서 닉네임으로 검색
    @Query("""
        SELECT f.follower FROM Follow f
        JOIN f.follower u
        WHERE f.followed.userId = :userId
        AND u.nickname LIKE %:nickname%
        AND u.isWithdraw = false
        ORDER BY u.likeCount DESC, u.nickname
        """)
    List<User> findFollowerUsersByNicknameContaining(@Param("userId") UUID userId, @Param("nickname") String nickname);

    // 단짝 친구 중에서 닉네임으로 검색
    @Query("""
        SELECT f.followed FROM Follow f
        JOIN f.followed u
        WHERE f.follower.userId = :userId
        AND f.isBestFriend = true
        AND u.nickname LIKE %:nickname%
        AND u.isWithdraw = false
        ORDER BY u.likeCount DESC, u.nickname
        """)
    List<User> findBestFriendUsersByNicknameContaining(@Param("userId") UUID userId, @Param("nickname") String nickname);

    // 내가 팔로우하고, 나를 팔로우하는 모든 유저 중에서 닉네임 검색
    @Query("""
    SELECT DISTINCT u FROM User u
    WHERE u.nickname LIKE %:nickname%
    AND u.isWithdraw = false
    AND u.userId IN (
        SELECT f1.followed.userId FROM Follow f1 WHERE f1.follower.userId = :userId
        UNION
        SELECT f2.follower.userId FROM Follow f2 WHERE f2.followed.userId = :userId
    )
    ORDER BY u.likeCount DESC, u.nickname
    """)
    List<User> findAllUsersByNicknameContaining(@Param("userId") UUID userId, @Param("nickname") String nickname);

    /**
     * 여러 사용자 ID로 UserAtlas 정보를 한 번에 조회
     */
    @Query("""
        SELECT ua FROM UserAtlas ua
        JOIN ua.user u
        JOIN ua.atlas a
        WHERE u.userId IN :userIds
        AND a.areaGroup = :areaGroup
        AND u.isWithdraw = false
        ORDER BY ua.level DESC, ua.currentExp DESC
        """)
    List<UserAtlas> findUserAtlasesByUserIdInAndAreaGroup(@Param("userIds") Set<UUID> userIds, @Param("areaGroup") AreaGroup areaGroup);
} 