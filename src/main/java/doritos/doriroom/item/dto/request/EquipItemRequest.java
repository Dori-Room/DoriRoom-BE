package doritos.doriroom.item.dto.request;

import jakarta.validation.constraints.NotBlank;

public record EquipItemRequest (
        @NotBlank
        Long itemId
){}
