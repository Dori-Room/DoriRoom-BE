package doritos.doriroom.item.dto.response;

import doritos.doriroom.item.domain.Item;
import lombok.Builder;

@Builder
public record PaymentViewResponse(
        String name,
        Long price,
        Long remainingCredit,
        boolean isBuyable // 보유 크레딧 상태에 따른 구매 가능 여부
)
{
    public static PaymentViewResponse from(Item item, Long remainingCredit){
        boolean isBuyable = remainingCredit > 0;

        return PaymentViewResponse.builder()
                .name(item.getName())
                .price(item.getPrice())
                .remainingCredit(remainingCredit)
                .isBuyable(isBuyable)
                .build();
    }
}
