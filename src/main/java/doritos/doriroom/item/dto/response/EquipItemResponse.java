package doritos.doriroom.item.dto.response;

import doritos.doriroom.item.domain.ItemType;
import lombok.Builder;

@Builder
public record EquipItemResponse(
        Long equippedItemId,
        String name,
        ItemType itemType,
        boolean isEquipped

) {
    private static EquipItemResponse from(Long equippedItemId, String name, ItemType itemType, boolean equipped) {
        return EquipItemResponse.builder()
                .equippedItemId(equippedItemId)
                .name(name)
                .itemType(itemType)
                .isEquipped(equipped)
                .build();
    }
}
