package doritos.doriroom.item.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import doritos.doriroom.item.domain.CollectionTheme;
import doritos.doriroom.item.domain.Item;
import doritos.doriroom.item.domain.ItemGroup;
import doritos.doriroom.item.domain.ItemType;
import doritos.doriroom.tourApi.domain.AreaGroup;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ItemResponse (
        Long itemId,
        String name,
//        String imageUrl,
        ItemType itemType,
        ItemGroup itemGroup, // COMMON or AREA
        AreaGroup areaGroup, // nullable
        CollectionTheme theme, // nullable
        Long price,
        boolean isPurchasable,
        boolean isOwned
){
    public static ItemResponse from(Item i, boolean isOwned) {
        return ItemResponse.builder()
                .itemId(i.getItemId())
                .name(i.getName())
//                .imageUrl(i.getImageUrl())
                .itemType(i.getItemType())
                .itemGroup(i.getItemGroup())
                .areaGroup(i.getAreaGroup())
                .price(i.getPrice())
                .theme(i.getTheme())
                .isPurchasable(i.isPurchasable())
                .isOwned(isOwned)
                .build();
    }
}
