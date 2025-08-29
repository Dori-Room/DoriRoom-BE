package doritos.doriroom.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record LocationFilterDto(
    @Schema(description = "지역그룹코드", example = "1")
    Integer areaGroupCode,

    @Schema(description = "지역코드", example = "1")
    Integer areaCode,

    @Schema(description = "시군구코드 (areaCode가 있어야 함)", example = "1")
    Integer sigunguCode
) {
}
