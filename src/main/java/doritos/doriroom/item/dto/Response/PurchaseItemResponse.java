package doritos.doriroom.item.dto.Response;

public record PurchaseItemResponse(
        Long itemId,
        String name,
        Long price,
        Long remainingCredit
){}
