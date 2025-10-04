package doritos.doriroom.challenge.controller;

import doritos.doriroom.challenge.domain.challenge.ChallengeGroup;
import doritos.doriroom.challenge.dto.response.ChallengeResponseDto;
import doritos.doriroom.challenge.service.ChallengeService;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
@Validated
public class ChallengeController {
    private final ChallengeService challengeService;

    @GetMapping("/group") // 과제그룹별 조회 - 일반 / 지역, 지역의 경우 특정 지역을 파라미터로 지정
    @Operation(summary = "지역별/일반 도전과제 리스트 조회", description = "")
    public ApiResponse<List<ChallengeResponseDto>> getChallengesByGroup(@AuthenticationPrincipal User user,
                                                                        @Parameter(description = "과제 그룹 (COMMON: 일반과제, AREA: 지역과제)", example = "COMMON", required = true)
                                                                        @RequestParam("group") @NotNull ChallengeGroup challengeGroup,
                                                                        @Parameter(description = "지역 그룹 (SEOUL, GYEONGGI, ...). challengeGroup이 AREA일 때 필요", example = "JEJU", required = false)
                                                                        @RequestParam(value = "area", required = false) AreaGroup areaGroup){
        return ApiResponse.ok(challengeService.getChallengesByGroup(user, challengeGroup, areaGroup));
    }

    @GetMapping("/{challengeId}")
    @Operation(summary = "도전과제 상세 정보 조회")
    public ApiResponse<ChallengeResponseDto> getChallengeDetail(@AuthenticationPrincipal User user,
                                                                @Parameter(description = "조회할 도전과제의 ID")
                                                                @PathVariable Long challengeId) {
        return ApiResponse.ok(challengeService.getChallengeDetail(user, challengeId));
    }

    @PostMapping("/{challengeId}/claim") // 해당 도전과제의 보상 받기 처리
    @Operation(summary = "특정 도전과제의 보상 받기 처리", description = "challengeId로 관련 리워드를 사용자에게 지급 및 도전과제 상태를 완료로 처리")
    public ApiResponse<Void> claimChallengeReward(@AuthenticationPrincipal User user,
                                                  @Parameter(description = "특정 도전과제의 ID", example = "2")
                                                  @PathVariable Long challengeId){
        challengeService.claimChallengeReward(user, challengeId);
        return ApiResponse.ok();
    }

    /* 축제 방문 과제 API */

    @PostMapping("/{challengeId}/start")
    @Operation(summary = "수동 시작 도전과제(축제 관련 또는 지역퀴즈) 상태 변경 (->도전 중)", description = "NOT_STARTED -> IN_PROGRESS")
    public ApiResponse<Void> startChallenge(@AuthenticationPrincipal User user,
                                            @Parameter(description = "특정 축제 관련 혹은 지역퀴즈 도전과제의 ID", example = "2")
                                            @PathVariable Long challengeId) {
        challengeService.startChallenge(user, challengeId);
        return ApiResponse.ok();
    }

    @PostMapping("/{challengeId}/complete")
    @Operation(summary = "축제 도전과제 완료 인증 후 도전과제 상태 변경 요청 (도전 중-> 보상 대기)", description = "IN_PROGRESS -> WAIT_REWARD")
    public ApiResponse<Void> completeChallenge(@AuthenticationPrincipal User user,
                                               @Parameter(description = "특정 축제 관련 도전과제의 ID", example = "2")
                                               @PathVariable Long challengeId) {
        challengeService.completeChallenge(user, challengeId);
        return ApiResponse.ok();
    }

}

