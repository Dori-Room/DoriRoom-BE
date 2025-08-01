package doritos.doriroom.item.dto.response;

import doritos.doriroom.item.domain.CollectionTheme;
import doritos.doriroom.item.domain.Item;
import doritos.doriroom.item.domain.ItemGroup;
import doritos.doriroom.item.domain.ItemType;
import lombok.Builder;

@Builder
public record ItemResponse (
        Long itemId,
        String name,
        String imageUrl,
        ItemType type,
        ItemGroup group,
        CollectionTheme theme, // nullable
        Long price,
        boolean isPurchasable,
        boolean isOwned
){
    public static ItemResponse from(Item i, boolean isOwned) {
        return ItemResponse.builder()
                .itemId(i.getItemId())
                .name(i.getName())
                .imageUrl(i.getImageUrl())
                .type(i.getType())
                .group(i.getGroup())
                .price(i.getPrice())
                .theme(i.getTheme())
                .isPurchasable(i.isPurchasable())
                .build();
    }
}
