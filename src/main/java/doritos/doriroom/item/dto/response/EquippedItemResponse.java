package doritos.doriroom.item.dto.response;

import doritos.doriroom.item.domain.Item;
import doritos.doriroom.item.domain.ItemType;
import doritos.doriroom.item.domain.UserItem;
import lombok.Builder;

@Builder
public record EquippedItemResponse (
        Long itemId,
//        String name,
        String imageUrl,
        ItemType itemType
) {
    public static EquippedItemResponse from(UserItem userItem) {
        Item item = userItem.getItem();

        return EquippedItemResponse.builder()
                .itemId(item.getItemId())
                .imageUrl(item.getImageUrl())
                .itemType(item.getItemType())
                .build();
    }
}
