package doritos.doriroom.challenge.controller;

import doritos.doriroom.challenge.domain.challenge.ChallengeGroup;
import doritos.doriroom.challenge.dto.response.ChallengeResponseDto;
import doritos.doriroom.challenge.service.ChallengeService;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {
    private final ChallengeService challengeService;

    @GetMapping("/group") // 과제그룹별 조회 - 일반 / 지역, 지역의 경우 특정 지역을 파라미터로 지정
    public ApiResponse<List<ChallengeResponseDto>> getChallengesByGroup(@AuthenticationPrincipal User user,
                                                          @Parameter(description = "과제 그룹 (COMMON: 일반과제, AREA: 지역과제)", example = "COMMON", required = true)
                                                          @RequestParam("group") ChallengeGroup challengeGroup,
                                                          @Parameter(description = "지역 그룹 (SEOUL, GYEONGGI, ...). challengeGroup이 AREA일 때 필요", example = "JEJU", required = false)
                                                          @RequestParam(value = "area", required = false) AreaGroup areaGroup){
        return ApiResponse.ok(challengeService.getChallengesByGroup(user, challengeGroup, areaGroup));
    }

}

