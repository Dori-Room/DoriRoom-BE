package doritos.doriroom.ranking.service;

import doritos.doriroom.follow.domain.Follow;
import doritos.doriroom.follow.repository.FollowRepository;
import doritos.doriroom.ranking.domain.FollowInfo;
import doritos.doriroom.ranking.dto.response.RankingSearchResponseDto;
import doritos.doriroom.ranking.repository.RankingRepository;
import doritos.doriroom.user.domain.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserSearchService {
    private final RankingRepository rankingRepository;
    private final FollowRepository followRepository;

    // 닉네임으로 전체 유저 검색
    public List<RankingSearchResponseDto> searchUsersInRanking(User currentUser, String nickname) {
        List<User> foundUsers = rankingRepository.findByNicknameContainingOrderByLikeCountDesc(nickname);

        if (foundUsers.isEmpty()) {
            return List.of();
        }

        FollowInfo followInfo = getFollowInfo(currentUser, foundUsers);

        // 검색 결과 DTO 변환
        List<RankingSearchResponseDto> searchResults = new ArrayList<>();

        for (User user : foundUsers) {
            Follow following = followInfo.followingMap().get(user.getUserId());
            boolean isFollowing = following != null;
            boolean isFollowedBy = followInfo.followedByUserIds().contains(user.getUserId());

            searchResults.add(RankingSearchResponseDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .following(isFollowing)
                .followedBy(isFollowedBy)
                .build());
        }
        return searchResults;
    }

    // 이웃도리 닉네임으로 유저 검색
    public List<RankingSearchResponseDto> searchFollowingUsers(User currentUser, String nickname) {
        List<User> foundFollowingUsers = rankingRepository.findFollowingUsersByNicknameContaining(
            currentUser.getUserId(), nickname);

        if (foundFollowingUsers.isEmpty()) {
            return List.of();
        }

        FollowInfo followInfo = getFollowInfo(currentUser, foundFollowingUsers);

        // 검색 결과 DTO 변환
        List<RankingSearchResponseDto> searchResults = new ArrayList<>();

        for (User user : foundFollowingUsers) {
            Follow following = followInfo.followingMap().get(user.getUserId());
            boolean isFollowing = following != null;
            boolean isFollowedBy = followInfo.followedByUserIds().contains(user.getUserId());

            searchResults.add(RankingSearchResponseDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .following(isFollowing)
                .followedBy(isFollowedBy)
                .build());
        }

        return searchResults;
    }

    // 현재 유저의 팔로우 정보를 조회
    private FollowInfo getFollowInfo(User currentUser, List<User> targetUsers) {
        if (targetUsers.isEmpty()) {
            return new FollowInfo(Collections.emptyMap(), Collections.emptySet());
        }

        Set<UUID> userIds = targetUsers.stream()
            .map(User::getUserId)
            .collect(Collectors.toSet());

        Map<UUID, Follow> followingMap = followRepository.findByFollowerAndFollowed_UserIdIn(currentUser, userIds)
            .stream()
            .collect(Collectors.toMap(follow -> follow.getFollowed().getUserId(), follow -> follow));

        Set<UUID> followedByUserIds = followRepository.findFollowerIdsByFollowedAndFollowerIdsIn(currentUser, userIds);

        return new FollowInfo(followingMap, followedByUserIds);
    }
}
