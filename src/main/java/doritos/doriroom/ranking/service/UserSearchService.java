package doritos.doriroom.ranking.service;

import doritos.doriroom.follow.domain.Follow;
import doritos.doriroom.follow.repository.FollowRepository;
import doritos.doriroom.item.dto.response.EquippedItemResponse;
import doritos.doriroom.item.service.ItemService;
import doritos.doriroom.ranking.domain.FollowInfo;
import doritos.doriroom.ranking.domain.SearchFilterType;
import doritos.doriroom.ranking.dto.response.RankingResponseDto;
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
    private final RankingService rankingService;
    private final ItemService itemService;

    // 닉네임으로 전체 유저 검색
    public List<RankingResponseDto> searchUsersInRanking(User currentUser, String nickname) {
        List<User> foundUsers = rankingRepository.findByNicknameContainingOrderByLikeCountDesc(nickname);

        if (foundUsers.isEmpty()) {
            return List.of();
        }

        FollowInfo followInfo = getFollowInfo(currentUser, foundUsers);

        // 검색된 유저들의 ID 수집
        Set<UUID> userIds = foundUsers.stream()
            .map(User::getUserId)
            .collect(Collectors.toSet());

        // 장착 아이템 정보 조회
        Map<UUID, List<EquippedItemResponse>> equippedItemsMap = itemService.getMultipleUsersEquippedItems(userIds);

        // 검색 결과 DTO 변환
        List<RankingResponseDto> searchResults = new ArrayList<>();

        for (User user : foundUsers) {
            Follow following = followInfo.followingMap().get(user.getUserId());
            boolean isFollowing = following != null;
            boolean isFollowedBy = followInfo.followedByUserIds().contains(user.getUserId());

            // 해당 유저의 장착 아이템 정보 가져오기
            List<EquippedItemResponse> equippedItems = equippedItemsMap.getOrDefault(user.getUserId(), List.of());

            String rank = rankingService.getUserRank(user.getUserId());

            searchResults.add(RankingResponseDto.builder()
                .rank(rank)
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .equippedItems(equippedItems)
                .following(isFollowing)
                .followedBy(isFollowedBy)
                .build());
        }
        return searchResults;
    }

    // 이웃도리 닉네임으로 유저 검색
    public List<RankingResponseDto> searchFollowingUsers(User currentUser, String nickname, SearchFilterType filterType) {
        List<User> foundUsers = switch (filterType) {
            case FOLLOWING ->
                rankingRepository.findFollowingUsersByNicknameContaining(currentUser.getUserId(),
                    nickname);
            case FOLLOWERS ->
                rankingRepository.findFollowerUsersByNicknameContaining(currentUser.getUserId(),
                    nickname);
            case BEST_FRIEND ->
                rankingRepository.findBestFriendUsersByNicknameContaining(currentUser.getUserId(),
                    nickname);
            default ->
                rankingRepository.findMutualFollowUsersByNicknameContaining(currentUser.getUserId(),
                    nickname);
        };

        if (foundUsers.isEmpty()) {
            return List.of();
        }

        FollowInfo followInfo = getFollowInfo(currentUser, foundUsers);

        // 검색된 유저들의 ID 수집
        Set<UUID> userIds = foundUsers.stream()
            .map(User::getUserId)
            .collect(Collectors.toSet());

        // 장착 아이템 정보 조회
        Map<UUID, List<EquippedItemResponse>> equippedItemsMap = itemService.getMultipleUsersEquippedItems(userIds);

        // 검색 결과 DTO 변환
        List<RankingResponseDto> searchResults = new ArrayList<>();

        for (User user : foundUsers) {
            Follow following = followInfo.followingMap().get(user.getUserId());
            boolean isFollowing = following != null;
            boolean isFollowedBy = followInfo.followedByUserIds().contains(user.getUserId());

            // 해당 유저의 장착 아이템 정보 가져오기
            List<EquippedItemResponse> equippedItems = equippedItemsMap.getOrDefault(user.getUserId(), List.of());

            String rank = rankingService.getUserRank(user.getUserId());

            searchResults.add(RankingResponseDto.builder()
                .rank(rank)
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .equippedItems(equippedItems)
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
