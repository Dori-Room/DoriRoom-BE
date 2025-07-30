package doritos.doriroom.item.dto.Request;

import doritos.doriroom.item.domain.ItemGroup;
import jakarta.validation.constraints.NotBlank;

public record ItemGroupRequest (
        @NotBlank
        ItemGroup group
){ }