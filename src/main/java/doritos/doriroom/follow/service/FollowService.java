package doritos.doriroom.follow.service;

import doritos.doriroom.follow.domain.Follow;
import doritos.doriroom.follow.dto.FollowFilterType;
import doritos.doriroom.follow.dto.request.FollowRequestDto;
import doritos.doriroom.follow.dto.request.UserSearchRequestDto;
import doritos.doriroom.follow.dto.response.*;
import doritos.doriroom.follow.exception.CannotFollowSelfException;
import doritos.doriroom.follow.exception.FollowAlreadyExistsException;
import doritos.doriroom.follow.exception.FollowNotFoundException;
import doritos.doriroom.follow.repository.FollowRepository;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.exception.UserNotFoundException;
import doritos.doriroom.user.repository.UserRepository;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    // targetUser 팔로우
    @Transactional
    public FollowResponseDto followUser(User user, FollowRequestDto request) {
        User targetUser = userRepository.findById(request.targetUserId())
                .orElseThrow(UserNotFoundException::new);

        // 본인은 팔로우 불가 예외
        if (user.getUserId().equals(request.targetUserId()))
            throw new CannotFollowSelfException();

        // 이미 팔로우 중인지 확인
        if (followRepository.existsByFollowerAndFollowed(user, targetUser))
            throw new FollowAlreadyExistsException();

        Follow follow = Follow.builder()
                .follower(user)
                .followed(targetUser)
                .build();

        followRepository.save(follow);

        //맞팔로우 여부 확인
//        boolean isMutual = followRepository.existsMutualFollow(user, targetUser);

        return FollowResponseDto.from(follow);
    }

    // 언팔로우
    @Transactional
    public void unfollowUser(User user, UUID targetUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(UserNotFoundException::new);

        // 팔로우 관계가 존재하는지 확인
        Follow follow = followRepository.findByFollowerAndFollowed(user, targetUser)
                .orElseThrow(FollowNotFoundException::new);

        followRepository.delete(follow); // 팔로우 관계 삭제
    }

    // 단짝 친구 설정 및 해제(토글(
    @Transactional
    public FollowResponseDto toggleBestFriend(User user, UUID targetUserId){
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(UserNotFoundException::new);

        // 팔로우 관계가 존재하는지 확인
        Follow follow = followRepository.findByFollowerAndFollowed(user, targetUser)
                .orElseThrow(FollowNotFoundException::new);

        follow.setBestFriend(!follow.isBestFriend()); // isBestFriend 값을 반전하여 반영
        return FollowResponseDto.from(follow);
    }

    // 유저와 특정 유저의 팔로우 상태 확인
    public FollowStatusResponseDto getFollowStatus(User user, UUID targetUserId){
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(UserNotFoundException::new);

        // 본인 조회 시도 시 Null 반환
        if (user.getUserId().equals(targetUserId))
            return new FollowStatusResponseDto(false, false, false, false);

        Follow following = followRepository.findByFollowerAndFollowed(user, targetUser).orElse(null);
        Follow followedBy = followRepository.findByFollowerAndFollowed(targetUser, user).orElse(null);

        return FollowStatusResponseDto.builder()
                .isFollowing(following != null) // 내가 팔로우
                .isFollowedBy(followedBy != null) // 타겟이 나를 팔로우
                .isBestFriend(following != null && following.isBestFriend()) // 단짝 여부
                .isMutualFollow(following != null && followedBy != null) // 맞팔 여부
                .build();
    }

    // 내가 팔로우 하는 유저 목록 조회 (팔로잉 목록)
    public FollowListResponseDto getFollowingList(User user, FollowFilterType filterType) {
        List<Follow> follows = switch (filterType) { // 필터링 적용, 기본값 최신순
            case RECENT -> followRepository.findByFollowerWithFollowedOrderByCreatedAtDesc(user);
            case OLDEST -> followRepository.findByFollowerWithFollowedOrderByCreatedAtAsc(user);
            case BEST_FRIEND -> followRepository.findBestFriendsByFollowerOrderByCreatedAtDesc(user);
        };

        // 맞팔로우 상태 유저 배치 조회
        Set<UUID> followedUserIds = follows.stream()
                .map(follow -> follow.getFollowed().getUserId())
                .collect(Collectors.toSet());
        Set<UUID> mutualFollowIds = followRepository.findMutualFollowUserIds(user.getUserId(), followedUserIds);


        // 내 팔로우 유저 정보 리스트 생성
        List<FollowUserInfoDto> followingUsers = follows.stream()
                .map(follow -> new FollowUserInfoDto(
                        follow.getFollowed().getUserId(),
                        follow.getFollowed().getNickname(),
                        follow.getFollowed().getProfileImageUrl(),
                        true,
                        mutualFollowIds.contains(follow.getFollowed().getUserId()), // isFollowedBy
                        follow.isBestFriend(),
                        follow.getCreatedAt()
                ))
                .toList();

        return new FollowListResponseDto(followingUsers, followingUsers.size());
    }

    // 나를 팔로우 하는 유저 목록 조회 (팔로워 목록)
    public FollowListResponseDto getFollowerList(User user, FollowFilterType filterType) {
//        if (filterType == FollowFilterType.BEST_FRIEND)
//            throw new ?Exception("팔로워 목록에는 단짝 친구 필터링을 사용할 수 없습니다.");

        List<Follow> follows = switch (filterType) {
            case OLDEST -> followRepository.findByFollowedWithFollowerOrderByCreatedAtAsc(user);
            case RECENT, BEST_FRIEND -> followRepository.findByFollowedWithFollowerOrderByCreatedAtDesc(user);
        };

        // 내가 팔로우하는 상태와 단짝 상태 배치 조회
        Set<UUID> followerUserIds = follows.stream()
                .map(follow -> follow.getFollower().getUserId())
                .collect(Collectors.toSet());
        Map<UUID, Follow> followingMap = followRepository.findByFollowerAndFollowed_UserIdIn(user, followerUserIds)
                .stream()
                .collect(Collectors.toMap(
                        follow -> follow.getFollowed().getUserId(),
                        follow -> follow
                ));

        // 팔로워 목록
        List<FollowUserInfoDto> followerUsers = follows.stream()
                .map(follow -> {
                    UUID followerId = follow.getFollower().getUserId();
                    Follow following = followingMap.get(followerId);

                    return new FollowUserInfoDto(
                            follow.getFollower().getUserId(),
                            follow.getFollower().getNickname(),
                            follow.getFollower().getProfileImageUrl(),
                            following != null, // isFollowing
                            true, // isFollowedBy
                            following != null && following.isBestFriend(),
                            follow.getCreatedAt()
                    );
                })
                .toList();

        return new FollowListResponseDto(followerUsers, followerUsers.size());
    }

}

