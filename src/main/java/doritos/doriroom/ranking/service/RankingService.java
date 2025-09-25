package doritos.doriroom.ranking.service;

import doritos.doriroom.atlas.domain.UserAtlas;
import doritos.doriroom.follow.domain.Follow;
import doritos.doriroom.follow.repository.FollowRepository;
import doritos.doriroom.ranking.domain.FollowInfo;
import doritos.doriroom.ranking.dto.response.RankingResponseDto;
import doritos.doriroom.ranking.dto.response.RegionalRankingResponseDto;
import doritos.doriroom.ranking.repository.RankingRepository;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import java.util.Collections;
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
import doritos.doriroom.ranking.domain.ProfileVisit;
import doritos.doriroom.ranking.repository.ProfileVisitRepository;
import doritos.doriroom.ranking.dto.response.RecentVisitResponseDto;
import org.springframework.data.redis.core.ZSetOperations;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RankingService {
    
    private final ZSetOperations<String, Object> zSetOperations;
    private final RankingRepository rankingRepository;
    private final FollowRepository followRepository;
    private final ProfileVisitRepository profileVisitRepository;
    
    // Redis 키 상수
    private static final String OVERALL_RANKING_KEY = "ranking:overall";
    private static final String REGIONAL_RANKING_KEY_PREFIX = "ranking:regional:";
    
    // 전체 랭킹 조회
    public List<RankingResponseDto> getAllRanking(User currentUser) {
        // Redis에서 상위 100명 조회
        Set<ZSetOperations.TypedTuple<Object>> rankingData = zSetOperations.reverseRangeWithScores(
            OVERALL_RANKING_KEY, 0, 99);
        
        if (rankingData == null || rankingData.isEmpty()) {
            log.warn("Redis에 랭킹 데이터가 없습니다. MySQL에서 조회합니다.");
            return getAllRankingFromMySQL(currentUser);
        }
        
        return buildOverallRankingResponse(rankingData, currentUser);
    }
    
    // 지역별 랭킹 조회
    public List<RegionalRankingResponseDto> getRegionalRanking(User currentUser, AreaGroup areaGroup) {
        String redisKey = REGIONAL_RANKING_KEY_PREFIX + areaGroup.name();
        
        // Redis에서 상위 100명 조회
        Set<ZSetOperations.TypedTuple<Object>> rankingData = zSetOperations.reverseRangeWithScores(
            redisKey, 0, 99);
        
        if (rankingData == null || rankingData.isEmpty()) {
            log.warn("Redis에 지역 랭킹 데이터가 없습니다. MySQL에서 조회합니다.");
            return getRegionalRankingFromMySQL(currentUser, areaGroup);
        }
        
        return buildRegionalRankingResponse(rankingData, currentUser, areaGroup);
    }
    
    // 내 전체 랭킹 조회
    public RankingResponseDto getMyAllRanking(User currentUser) {
        // Redis에서 내 순위 조회
        Long rank = zSetOperations.reverseRank(OVERALL_RANKING_KEY, currentUser.getUserId().toString());
        
        if (rank == null) {
            log.warn("Redis에 사용자 랭킹 데이터가 없습니다. MySQL에서 조회합니다.");
            return getMyAllRankingFromMySQL(currentUser);
        }
        
        // Redis에서 내 점수 조회
        Double score = zSetOperations.score(OVERALL_RANKING_KEY, currentUser.getUserId().toString());
        long likeCount = score != null ? score.longValue() : 0;
        
        // 팔로우 관계 조회
        boolean following = false;
        boolean followedBy = false;
        
        return RankingResponseDto.builder()
            .rank(rank == 0 ? "1" : String.valueOf(rank + 1))
            .userId(currentUser.getUserId())
            .nickname(currentUser.getNickname())
            .profileImageUrl(currentUser.getProfileImageUrl())
            .likeCount((int) likeCount)
            .following(following)
            .followedBy(followedBy)
            .build();
    }
    
    // 내 지역별 랭킹 조회
    public RegionalRankingResponseDto getMyRegionalRanking(User currentUser, AreaGroup areaGroup) {
        String redisKey = REGIONAL_RANKING_KEY_PREFIX + areaGroup.name();
        
        // Redis에서 내 순위 조회
        Long rank = zSetOperations.reverseRank(redisKey, currentUser.getUserId().toString());
        
        if (rank == null) {
            log.warn("Redis에 사용자 지역 랭킹 데이터가 없습니다. MySQL에서 조회합니다.");
            return getMyRegionalRankingFromMySQL(currentUser, areaGroup);
        }
        
        // Redis에서 내 점수 조회 (도감 레벨 * 10000 + 경험치)
        Double score = zSetOperations.score(redisKey, currentUser.getUserId().toString());
        long totalScore = score != null ? score.longValue() : 0;
        
        int atlasLevel = (int) (totalScore / 10000);
        int atlasExp = (int) (totalScore % 10000);
        
        return RegionalRankingResponseDto.builder()
            .rank(rank == 0 ? "1" : String.valueOf(rank + 1))
            .userId(currentUser.getUserId())
            .nickname(currentUser.getNickname())
            .profileImageUrl(currentUser.getProfileImageUrl())
            .atlasLevel(atlasLevel)
            .atlasExp(atlasExp)
            .areaGroup(areaGroup)
            .build();
    }
    
    // 좋아요 수 업데이트
    @Transactional
    public void updateLikeCount(UUID userId, int newLikeCount) {
        // 전체 랭킹 업데이트
        zSetOperations.add(OVERALL_RANKING_KEY, userId.toString(), newLikeCount);
    }
    
    // 지역별 랭킹 업데이트
    @Transactional
    public void updateRegionalRanking(UUID userId, AreaGroup areaGroup, int atlasLevel, Long atlasExp) {
        String redisKey = REGIONAL_RANKING_KEY_PREFIX + areaGroup.name();
        
        // 도감 레벨 * 10000 + 경험치로 점수 계산
        double score = atlasLevel * 10000.0 + atlasExp;
        
        zSetOperations.add(redisKey, userId.toString(), score);
    }
    
    // Redis 랭킹 데이터 초기화
    @Transactional
    public void initializeRankingData() {
        log.info("Redis 랭킹 데이터 초기화를 시작합니다.");
        
        // 전체 랭킹 데이터 초기화
        initializeOverallRanking();
        
        // 지역별 랭킹 데이터 초기화
        for (AreaGroup areaGroup : AreaGroup.values()) {
            initializeRegionalRanking(areaGroup);
        }
        
        log.info("Redis 랭킹 데이터 초기화가 완료되었습니다.");
    }
    
    // ========== MySQL 조회 메서드들 (Fallback) ==========
    
    // MySQL에서 전체 랭킹 조회
    private List<RankingResponseDto> getAllRankingFromMySQL(User currentUser) {
        List<User> topUsers = rankingRepository.findTop100ByOrderByLikeCountDesc();
        
        // 팔로우 관계 정보 조회
        Set<UUID> userIds = topUsers.stream()
            .map(User::getUserId)
            .collect(Collectors.toSet());
        
        Map<UUID, Follow> followingMap = followRepository.findByFollowerAndFollowed_UserIdIn(currentUser, userIds)
            .stream()
            .collect(Collectors.toMap(follow -> follow.getFollowed().getUserId(), follow -> follow));
        
        Set<UUID> followedByMeUserIds = followRepository.findFollowerIdsByFollowedAndFollowerIdsIn(currentUser, userIds);
        
        // 랭킹 응답 DTO 변환
        List<RankingResponseDto> rankingList = new ArrayList<>();
        
        for (int i = 0; i < topUsers.size(); i++) {
            User user = topUsers.get(i);
            Follow following = followingMap.get(user.getUserId());
            boolean isFollowing = following != null;
            boolean isFollowedBy = followedByMeUserIds.contains(user.getUserId());
            
            rankingList.add(RankingResponseDto.builder()
                .rank(String.valueOf(i + 1))
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .likeCount(user.getLikeCount())
                .following(isFollowing)
                .followedBy(isFollowedBy)
                .build());
        }
        
        return rankingList;
    }
    
    // MySQL에서 지역별 랭킹 조회
    private List<RegionalRankingResponseDto> getRegionalRankingFromMySQL(User currentUser, AreaGroup areaGroup) {
        List<UserAtlas> topUsers = rankingRepository.findTop100ByAreaGroupOrderByLevelDescAndExpDesc(areaGroup);
        
        // 팔로우 관계 정보 조회
        Set<UUID> userIds = topUsers.stream()
            .map(userAtlas -> userAtlas.getUser().getUserId())
            .collect(Collectors.toSet());
        
        Map<UUID, Follow> followingMap = followRepository.findByFollowerAndFollowed_UserIdIn(currentUser, userIds)
            .stream()
            .collect(Collectors.toMap(follow -> follow.getFollowed().getUserId(), follow -> follow));
        
        Set<UUID> followedByMeUserIds = followRepository.findFollowerIdsByFollowedAndFollowerIdsIn(currentUser, userIds);
        
        // 랭킹 응답 DTO 변환
        List<RegionalRankingResponseDto> rankingList = new ArrayList<>();
        
        for (int i = 0; i < topUsers.size(); i++) {
            UserAtlas userAtlas = topUsers.get(i);
            User user = userAtlas.getUser();
            Follow following = followingMap.get(user.getUserId());
            boolean isFollowing = following != null;
            boolean isFollowedBy = followedByMeUserIds.contains(user.getUserId());
            
            rankingList.add(RegionalRankingResponseDto.builder()
                .rank(String.valueOf(i + 1))
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .atlasLevel(userAtlas.getLevel())
                .atlasExp(Math.toIntExact(userAtlas.getCurrentExp()))
                .areaGroup(areaGroup)
                .following(isFollowing)
                .followedBy(isFollowedBy)
                .build());
        }
        
        return rankingList;
    }
    
    // MySQL에서 내 전체 랭킹 조회
    private RankingResponseDto getMyAllRankingFromMySQL(User currentUser) {
        Integer myRank = rankingRepository.findMyDenseRankByUserId(currentUser.getUserId());
        Long myLikeCount = rankingRepository.findLikeCountByUserId(currentUser.getUserId()).orElse(0L);
        
        return RankingResponseDto.builder()
            .rank(myRank != null ? String.valueOf(myRank) : "-")
            .userId(currentUser.getUserId())
            .nickname(currentUser.getNickname())
            .profileImageUrl(currentUser.getProfileImageUrl())
            .likeCount(myLikeCount.intValue())
            .following(false)
            .followedBy(false)
            .build();
    }
    
    // MySQL에서 내 지역별 랭킹 조회
    private RegionalRankingResponseDto getMyRegionalRankingFromMySQL(User currentUser, AreaGroup areaGroup) {
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
    
    // ========== Redis 응답 빌드 메서드들 ==========
    
    // Redis 데이터로 전체 랭킹 응답 빌드
    private List<RankingResponseDto> buildOverallRankingResponse(Set<ZSetOperations.TypedTuple<Object>> rankingData, User currentUser) {
        // Redis 데이터를 User 객체로 변환하고 팔로우 관계 조회
        List<User> users = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : rankingData) {
            String userIdStr = (String) tuple.getValue();
            UUID userId = UUID.fromString(userIdStr);
            User user = rankingRepository.findById(userId).orElse(null);
            if (user != null) {
                users.add(user);
            }
        }
        
        // 팔로우 관계 정보 조회
        Set<UUID> userIds = users.stream()
            .map(User::getUserId)
            .collect(Collectors.toSet());
        
        Map<UUID, Follow> followingMap = followRepository.findByFollowerAndFollowed_UserIdIn(currentUser, userIds)
            .stream()
            .collect(Collectors.toMap(follow -> follow.getFollowed().getUserId(), follow -> follow));
        
        Set<UUID> followedByMeUserIds = followRepository.findFollowerIdsByFollowedAndFollowerIdsIn(currentUser, userIds);
        
        // 랭킹 응답 DTO 변환
        List<RankingResponseDto> rankingList = new ArrayList<>();
        
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            Follow following = followingMap.get(user.getUserId());
            boolean isFollowing = following != null;
            boolean isFollowedBy = followedByMeUserIds.contains(user.getUserId());
            
            rankingList.add(RankingResponseDto.builder()
                .rank(String.valueOf(i + 1))
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .likeCount(user.getLikeCount())
                .following(isFollowing)
                .followedBy(isFollowedBy)
                .build());
        }
        
        return rankingList;
    }
    
    // Redis 데이터로 지역별 랭킹 응답 빌드
    private List<RegionalRankingResponseDto> buildRegionalRankingResponse(Set<ZSetOperations.TypedTuple<Object>> rankingData, User currentUser, AreaGroup areaGroup) {
        // Redis 데이터를 UserAtlas 객체로 변환하고 팔로우 관계 조회
        List<UserAtlas> userAtlases = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : rankingData) {
            String userIdStr = (String) tuple.getValue();
            UUID userId = UUID.fromString(userIdStr);
            Optional<UserAtlas> userAtlasOpt = rankingRepository.findUserAtlasByUserIdAndAreaGroup(userId, areaGroup);
            if (userAtlasOpt.isPresent()) {
                userAtlases.add(userAtlasOpt.get());
            }
        }
        
        // 팔로우 관계 정보 조회
        Set<UUID> userIds = userAtlases.stream()
            .map(userAtlas -> userAtlas.getUser().getUserId())
            .collect(Collectors.toSet());
        
        Map<UUID, Follow> followingMap = followRepository.findByFollowerAndFollowed_UserIdIn(currentUser, userIds)
            .stream()
            .collect(Collectors.toMap(follow -> follow.getFollowed().getUserId(), follow -> follow));
        
        Set<UUID> followedByMeUserIds = followRepository.findFollowerIdsByFollowedAndFollowerIdsIn(currentUser, userIds);
        
        // 랭킹 응답 DTO 변환
        List<RegionalRankingResponseDto> rankingList = new ArrayList<>();
        
        for (int i = 0; i < userAtlases.size(); i++) {
            UserAtlas userAtlas = userAtlases.get(i);
            User user = userAtlas.getUser();
            Follow following = followingMap.get(user.getUserId());
            boolean isFollowing = following != null;
            boolean isFollowedBy = followedByMeUserIds.contains(user.getUserId());
            
            rankingList.add(RegionalRankingResponseDto.builder()
                .rank(String.valueOf(i + 1))
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .atlasLevel(userAtlas.getLevel())
                .atlasExp(Math.toIntExact(userAtlas.getCurrentExp()))
                .areaGroup(areaGroup)
                .following(isFollowing)
                .followedBy(isFollowedBy)
                .build());
        }
        
        return rankingList;
    }
    
    // ========== Redis 초기화 메서드들 ==========
    
    private void initializeOverallRanking() {
        List<User> topUsers = rankingRepository.findTop100ByOrderByLikeCountDesc();
        
        for (User user : topUsers) {
            zSetOperations.add(OVERALL_RANKING_KEY, user.getUserId().toString(), user.getLikeCount());
        }
        
        log.info("전체 랭킹 데이터 초기화 완료: {}명", topUsers.size());
    }
    
    private void initializeRegionalRanking(AreaGroup areaGroup) {
        String redisKey = REGIONAL_RANKING_KEY_PREFIX + areaGroup.name();
        
        List<UserAtlas> topUsers = rankingRepository.findTop100ByAreaGroupOrderByLevelDescAndExpDesc(areaGroup);
        
        for (UserAtlas userAtlas : topUsers) {
            double score = userAtlas.getLevel() * 10000.0 + userAtlas.getCurrentExp();
            zSetOperations.add(redisKey, userAtlas.getUser().getUserId().toString(), score);
        }
        
        log.info("지역 랭킹 데이터 초기화 완료: {} - {}명", areaGroup, topUsers.size());
    }
} 