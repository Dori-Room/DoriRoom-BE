package doritos.doriroom.item.dto.Request;

import jakarta.validation.constraints.NotBlank;

public record PurchaseItemRequest (

        @NotBlank
        Long itemId
){}
