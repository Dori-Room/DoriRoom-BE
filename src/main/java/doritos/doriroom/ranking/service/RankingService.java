package doritos.doriroom.ranking.service;

import doritos.doriroom.atlas.domain.UserAtlas;
import doritos.doriroom.follow.domain.Follow;
import doritos.doriroom.follow.repository.FollowRepository;
import doritos.doriroom.ranking.dto.response.RankingResponseDto;
import doritos.doriroom.ranking.dto.response.RankingSearchResponseDto;
import doritos.doriroom.ranking.dto.response.RegionalRankingResponseDto;
import doritos.doriroom.ranking.repository.RankingRepository;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RankingService {
    
    private final RankingRepository rankingRepository;
    private final FollowRepository followRepository;
    
    // 전체 랭킹 조회 (상위 100명)
    public List<RankingResponseDto> getAllRanking(User currentUser) {
        List<User> topUsers = rankingRepository.findTop100ByOrderByLikeCountDesc();
        
        if (topUsers.isEmpty()) {
            return List.of();
        }
        
        // 팔로우 관계 정보 조회
        Set<UUID> userIds = topUsers.stream()
            .map(User::getUserId)
            .collect(Collectors.toSet());
        
        // 내가 팔로우하는 유저들
        Map<UUID, Follow> followingMap = followRepository.findByFollowerAndFollowed_UserIdIn(currentUser, userIds)
            .stream()
            .collect(Collectors.toMap(follow -> follow.getFollowed().getUserId(), follow -> follow));
        
        // 나를 팔로우하는 유저들
        Set<UUID> followedByMeUserIds = followRepository.findFollowerIdsByFollowedAndFollowerIdsIn(currentUser, userIds);
        
        // 랭킹 계산 및 DTO 변환
        List<RankingResponseDto> rankings = new ArrayList<>();
        int currentRank = 1;

        for (int i = 0; i < topUsers.size(); i++) {
            User user = topUsers.get(i);
            
            // 같은 좋아요 수가 아니면 등수 업데이트
            if (i > 0 && user.getLikeCount() < topUsers.get(i - 1).getLikeCount()) {
                currentRank++;
            }
            
            Follow following = followingMap.get(user.getUserId());
            boolean isFollowing = following != null;
            boolean isFollowedBy = followedByMeUserIds.contains(user.getUserId());
            
            // 등수가 0이면 "-"로 표시, 아니면 숫자로 표시
            String rankDisplay = user.getLikeCount() == 0 ? "-" : String.valueOf(currentRank);

            rankings.add(RankingResponseDto.builder()
                .rank(rankDisplay)
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .likeCount(user.getLikeCount())
                .following(isFollowing)
                .followedBy(isFollowedBy)
                .build());
        }
        return rankings;
    }
    
    // 지역별 랭킹 조회 (상위 100명)
    public List<RegionalRankingResponseDto> getRegionalRanking(User currentUser, AreaGroup areaGroup) {
        List<UserAtlas> topUserAtlases = rankingRepository.findTop100ByAreaGroupOrderByLevelDescAndExpDesc(areaGroup);
        
        if (topUserAtlases.isEmpty()) {
            return List.of();
        }
        
        // 유저 정보 추출
        List<User> topUsers = topUserAtlases.stream()
            .map(UserAtlas::getUser)
            .toList();
        
        // 팔로우 관계 정보 조회
        Set<UUID> userIds = topUsers.stream()
            .map(User::getUserId)
            .collect(Collectors.toSet());
        
        // 내가 팔로우하는 유저들
        Map<UUID, Follow> followingMap = followRepository.findByFollowerAndFollowed_UserIdIn(currentUser, userIds)
            .stream()
            .collect(Collectors.toMap(follow -> follow.getFollowed().getUserId(), follow -> follow));
        
        // 나를 팔로우하는 유저들
        Set<UUID> followedByMeUserIds = followRepository.findFollowerIdsByFollowedAndFollowerIdsIn(currentUser, userIds);
        
        // 랭킹 계산 및 DTO 변환
        List<RegionalRankingResponseDto> rankings = new ArrayList<>();
        int currentRank = 1;
        int previousLevel = -1;
        long previousExp = -1;
        
        for (int i = 0; i < topUserAtlases.size(); i++) {
            UserAtlas userAtlas = topUserAtlases.get(i);
            User user = userAtlas.getUser();
            
            // 같은 레벨과 경험치가 아니면 등수 업데이트
            if (previousLevel != -1 && (userAtlas.getLevel() != previousLevel || userAtlas.getCurrentExp() != previousExp)) {
                currentRank = i + 1;
            }
            
            Follow following = followingMap.get(user.getUserId());
            boolean isFollowing = following != null;
            boolean isFollowedBy = followedByMeUserIds.contains(user.getUserId());
            
            // 도감 레벨이 0이면 "-"로 표시, 아니면 숫자로 표시
            String rankDisplay = userAtlas.getLevel() == 0 ? "-" : String.valueOf(currentRank);
            
            rankings.add(RegionalRankingResponseDto.builder()
                .rank(rankDisplay)
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .atlasLevel(userAtlas.getLevel())
                .atlasExp(userAtlas.getCurrentExp().intValue())
                .areaGroup(areaGroup)
                .following(isFollowing)
                .followedBy(isFollowedBy)
                .build());
            
            previousLevel = userAtlas.getLevel();
            previousExp = userAtlas.getCurrentExp();
        }
        
        return rankings;
    }
    
    // 내 전체 랭킹 조회
    public RankingResponseDto getMyAllRanking(User user) {
        long myLikeCount = rankingRepository.findLikeCountByUserId(user.getUserId())
            .orElse(0L);
        Integer myRank = rankingRepository.findMyDenseRankByUserId(user.getUserId());

        String rankDisplay = myLikeCount == 0 ? "-" : String.valueOf(myRank);

        return RankingResponseDto.builder()
            .rank(rankDisplay)
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .profileImageUrl(user.getProfileImageUrl())
            .likeCount(user.getLikeCount())
            .build();
    }

    // 내 지역별 랭킹 조회
    public RegionalRankingResponseDto getMyRegionalRanking(User currentUser, AreaGroup areaGroup) {
        // 내 지역별 도감 정보 조회
        Optional<UserAtlas> myUserAtlasOpt = rankingRepository.findUserAtlasByUserIdAndAreaGroup(
            currentUser.getUserId(), areaGroup);

        // 해당 지역에 내 기록이 없는 경우 순위 없음 반환
        if (myUserAtlasOpt.isEmpty()) {
            return RegionalRankingResponseDto.builder()
                .rank("-")
                .userId(currentUser.getUserId())
                .nickname(currentUser.getNickname())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .atlasLevel(0)
                .atlasExp(0)
                .areaGroup(areaGroup)
                .build();
        }

        UserAtlas myUserAtlas = myUserAtlasOpt.get();

        // 내 기록이 0점인 경우 순위 없음 반환
        if (myUserAtlas.getLevel() == 0) {
            return RegionalRankingResponseDto.builder()
                .rank("-")
                .userId(currentUser.getUserId())
                .nickname(currentUser.getNickname())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .atlasLevel(0)
                .atlasExp(0)
                .areaGroup(areaGroup)
                .build();
        }

        // 내 랭킹 조회
        Long myRank = rankingRepository.findMyRankByScore(
            areaGroup,
            myUserAtlas.getLevel(),
            myUserAtlas.getCurrentExp()
        );

        return RegionalRankingResponseDto.builder()
            .rank(String.valueOf(myRank))
            .userId(currentUser.getUserId())
            .nickname(currentUser.getNickname())
            .profileImageUrl(currentUser.getProfileImageUrl())
            .atlasLevel(myUserAtlas.getLevel())
            .atlasExp(myUserAtlas.getCurrentExp().intValue())
            .areaGroup(areaGroup)
            .build();
    }

    // 닉네임으로 전체 유저 검색
    public List<RankingSearchResponseDto> searchUsersInRanking(User currentUser, String nickname) {
        List<User> foundUsers = rankingRepository.findByNicknameContainingOrderByLikeCountDesc(nickname);

        if (foundUsers.isEmpty()) {
            return List.of();
        }

        // 팔로우 관계 정보 조회
        Set<UUID> userIds = foundUsers.stream()
            .map(User::getUserId)
            .collect(Collectors.toSet());

        // 내가 팔로우하는 유저들
        Map<UUID, Follow> followingMap = followRepository.findByFollowerAndFollowed_UserIdIn(currentUser, userIds)
            .stream()
            .collect(Collectors.toMap(follow -> follow.getFollowed().getUserId(), follow -> follow));

        // 나를 팔로우하는 유저들
        Set<UUID> followedByMeUserIds = followRepository.findFollowerIdsByFollowedAndFollowerIdsIn(currentUser, userIds);

        // 검색 결과 DTO 변환
        List<RankingSearchResponseDto> searchResults = new ArrayList<>();

        for (User user : foundUsers) {
            Follow following = followingMap.get(user.getUserId());
            boolean isFollowing = following != null;
            boolean isFollowedBy = followedByMeUserIds.contains(user.getUserId());

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

        // 팔로우 관계 정보 조회
        Set<UUID> userIds = foundFollowingUsers.stream()
            .map(User::getUserId)
            .collect(Collectors.toSet());

        // 내가 팔로우하는 유저들
        Map<UUID, Follow> followingMap = followRepository.findByFollowerAndFollowed_UserIdIn(currentUser, userIds)
            .stream()
            .collect(Collectors.toMap(follow -> follow.getFollowed().getUserId(), follow -> follow));

        // 나를 팔로우하는 유저들
        Set<UUID> followedByMeUserIds = followRepository.findFollowerIdsByFollowedAndFollowerIdsIn(currentUser, userIds);

        // 검색 결과 DTO 변환
        List<RankingSearchResponseDto> searchResults = new ArrayList<>();

        for (User user : foundFollowingUsers) {
            Follow following = followingMap.get(user.getUserId());
            boolean isFollowing = following != null;
            boolean isFollowedBy = followedByMeUserIds.contains(user.getUserId());

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
} 