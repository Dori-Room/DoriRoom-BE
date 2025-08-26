package doritos.doriroom.follow.repository;

import doritos.doriroom.follow.domain.Follow;
import doritos.doriroom.user.domain.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<Follow, UUID> {
    boolean existsByFollowerAndFollowed(User follower, User followed); // 팔로우 관계 확인 (팔로우 버튼 상태 설정)
    Optional<Follow> findByFollowerAndFollowed(User follower, User followed); // 팔로우 관계 조회

    // 내가 팔로우하는 사람들 조회
    @EntityGraph(attributePaths = {"followed"}) // 한 번에 조회하도록 설정
//    @Query(value = "SELECT f FROM Follow f JOIN FETCH f.followed WHERE f.follower = :follower", countQuery = "SELECT count(f) FROM Follow f WHERE f.follower = :follower")
    Page<Follow> findByFollower(User follower, Pageable pageable);

    // 나를 팔로우하는 사람들 조회 (팔로워 목록)
    @EntityGraph(attributePaths = {"follower"})
//    @Query(value = "SELECT f FROM Follow f JOIN FETCH f.follower WHERE f.followed = :followed", countQuery = "SELECT count(f) FROM Follow f WHERE f.followed = :followed")
    Page<Follow> findByFollowed(User followed, Pageable pageable);

    // 단짝 친구들만 조회
    @EntityGraph(attributePaths = {"followed"})
//    @Query(value = "SELECT f FROM Follow f JOIN FETCH f.followed WHERE f.follower = :follower AND f.isBestFriend = true", countQuery = "SELECT count(f) FROM Follow f WHERE f.follower = :follower AND f.isBestFriend = true")
    Page<Follow> findByFollowerAndIsBestFriendTrue(User follower, Pageable pageable);


    // 특정 FollowerId(검색된 유저들) 중에서 나를 팔로우 하는 유저 목록
    @Query("SELECT f.follower.userId FROM Follow f WHERE f.followed = :followed AND f.follower.userId IN :followerIds")
    Set<UUID> findFollowerIdsByFollowedAndFollowerIdsIn(@Param("followed") User followed, @Param("followerIds") Set<UUID> followerIds);

    // 유저가 팔로우 하는 사람 목록 중 나를 팔로우 하는 유저 목록(맞팔로우)
    @Query("SELECT f.follower.userId FROM Follow f WHERE f.followed.userId = :userId AND f.follower.userId IN :followedUserIds")
    Set<UUID> findMutualFollowUserIds(@Param("userId") UUID userId, @Param("followedUserIds") Set<UUID> followedUserIds);

    List<Follow> findByFollowerAndFollowed_UserIdIn(User user, Set<UUID> targetUserIds); // 팔로우 정보 추출

}
