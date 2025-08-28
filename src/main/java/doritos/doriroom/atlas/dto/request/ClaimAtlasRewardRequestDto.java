package doritos.doriroom.atlas.dto.request;

import jakarta.validation.constraints.NotNull;

public record ClaimAtlasRewardRequestDto(
        @NotNull Long atlasRewardId
){ }
