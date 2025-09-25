package doritos.doriroom.ranking.controller;

import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.ranking.dto.response.RankingResponseDto;
import doritos.doriroom.ranking.dto.response.RankingSearchResponseDto;
import doritos.doriroom.ranking.dto.response.RegionalRankingResponseDto;
import doritos.doriroom.ranking.dto.response.RecentVisitResponseDto;
import doritos.doriroom.ranking.service.RankingService;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "랭킹", description = "Ranking API")
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {
    
    private final RankingService rankingService;
    
    @Operation(summary = "전체 랭킹 조회", description = "방 좋아요 수 기준 상위 100명 랭킹 조회")
    @GetMapping("/all")
    public ApiResponse<List<RankingResponseDto>> getOverallRanking(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(rankingService.getAllRanking(user));
    }

    @Operation(summary = "내 전체 랭킹 조회", description = "전체 랭킹에서 내 순위 조회")
    @GetMapping("/my-all")
    public ApiResponse<RankingResponseDto> getMyOverallRanking(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(rankingService.getMyAllRanking(user));
    }

    @Operation(summary = "지역별 랭킹 조회", description = "지역별 도감 레벨, 경험치 기준 상위 100명 랭킹 조회")
    @GetMapping("/regional")
    public ApiResponse<List<RegionalRankingResponseDto>> getRegionalRanking(
        @AuthenticationPrincipal User user,
        @RequestParam AreaGroup areaGroup) {
        return ApiResponse.ok(rankingService.getRegionalRanking(user, areaGroup));
    }

    @Operation(summary = "내 지역별 랭킹 조회", description = "지역별 랭킹에서 내 순위 조회")
    @GetMapping("/my-regional")
    public ApiResponse<RegionalRankingResponseDto> getMyRegionalRanking(
        @AuthenticationPrincipal User user,
        @RequestParam AreaGroup areaGroup) {
        return ApiResponse.ok(rankingService.getMyRegionalRanking(user, areaGroup));
    }

    @Operation(summary = "전체 유저 검색", description = "닉네임으로 전체 유저 중에서 검색")
    @GetMapping("/search/all")
    public ApiResponse<List<RankingSearchResponseDto>> searchUsersInRanking(
        @AuthenticationPrincipal User user,
        @Parameter(description = "닉네임", example = "도리", required = true)
        @RequestParam String nickname) {
        return ApiResponse.ok(rankingService.searchUsersInRanking(user, nickname));
    }

    @Operation(summary = "이웃도리 유저 검색", description = "닉네임으로 내가 팔로우하는 유저 내에서만 검색")
    @GetMapping("/search/follow")
    public ApiResponse<List<RankingSearchResponseDto>> searchFollowingUsers(
        @AuthenticationPrincipal User user,
        @Parameter(description = "닉네임", example = "도리", required = true)
        @RequestParam String nickname) {
        return ApiResponse.ok(rankingService.searchFollowingUsers(user, nickname));
    }

    @Operation(summary = "최근 방문한 프로필 조회", description = "최근 방문한 프로필 목록을 최신순으로 조회 (최대 20개)")
    @GetMapping("/recent-visits")
    public ApiResponse<List<RecentVisitResponseDto>> getRecentVisits(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(rankingService.getRecentVisits(user));
    }
}