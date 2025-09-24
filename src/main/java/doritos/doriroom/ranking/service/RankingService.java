package doritos.doriroom.ranking.service;

import doritos.doriroom.follow.domain.Follow;
import doritos.doriroom.follow.repository.FollowRepository;
import doritos.doriroom.ranking.dto.response.RankingResponseDto;
import doritos.doriroom.ranking.repository.RankingRepository;
import doritos.doriroom.user.domain.User;
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
        int previousLikeCount = -1;
        
        for (int i = 0; i < topUsers.size(); i++) {
            User user = topUsers.get(i);
            
            // 같은 좋아요 수가 아니면 등수 업데이트
            if (previousLikeCount != -1 && user.getLikeCount() != previousLikeCount) {
                currentRank = i + 1;
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
            
            previousLikeCount = user.getLikeCount();
        }
        
        return rankings;
    }
} 