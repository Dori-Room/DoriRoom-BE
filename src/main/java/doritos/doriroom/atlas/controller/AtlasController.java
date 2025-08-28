package doritos.doriroom.atlas.controller;


import doritos.doriroom.atlas.dto.request.ClaimAtlasRewardRequestDto;
import doritos.doriroom.atlas.dto.response.AtlasResponseDto;
import doritos.doriroom.atlas.service.AtlasService;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/atlases")
@RequiredArgsConstructor
public class AtlasController {
    private final AtlasService atlasService;


    @GetMapping
    @Operation(summary = "유저의 지역 도감들을 조회", description = "areaGroup 파라미터가 없으면 전체 조회, 있으면 특정 지역만 조회합니다.")
    public ApiResponse<List<AtlasResponseDto>> getAtlases(@AuthenticationPrincipal User user,
                                                          @RequestParam(value = "areaGroup", required = false) AreaGroup areaGroup) {
        return ApiResponse.ok(atlasService.getAtlases(user, areaGroup));
    }

    @PostMapping("/{atlasRewardId}/claim")
    @Operation(summary = "지역도감 보상 수령", description = "레벨 조건을 만족한 도감 보상을 수령")
    public ApiResponse<Void> claimReward(@AuthenticationPrincipal User user, @PathVariable Long atlasRewardId){
        atlasService.claimAtlasReward(user, atlasRewardId);
        return ApiResponse.ok();
    }


}
