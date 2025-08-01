package doritos.doriroom.item.dto.request;

import jakarta.validation.constraints.NotNull;

public record EquipItemRequest (
        @NotNull Long itemId
){}
