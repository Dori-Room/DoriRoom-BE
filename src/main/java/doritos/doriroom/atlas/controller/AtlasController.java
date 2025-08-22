package doritos.doriroom.atlas.controller;


import doritos.doriroom.atlas.dto.response.AtlasResponseDto;
import doritos.doriroom.atlas.service.AtlasService;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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


}
