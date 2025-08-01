package doritos.doriroom.item.dto.response;

public record PurchaseItemResponse(
        Long itemId,
        String name,
        Long price,
        Long remainingCredit
){}
