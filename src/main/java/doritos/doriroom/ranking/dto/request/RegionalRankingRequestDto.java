package doritos.doriroom.ranking.dto.request;

import doritos.doriroom.tourApi.domain.AreaGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record RegionalRankingRequestDto(
    @Schema(description = "지역 그룹", example = "SEOUL")
    AreaGroup areaGroup
) {
} 