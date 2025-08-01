package doritos.doriroom.item.dto.response;

import doritos.doriroom.item.domain.ItemType;
import lombok.Builder;

@Builder
public record EquipItemResponse(
        Long equippedItemId,
        String name,
        ItemType type,
        boolean isEquipped

) {
    private static EquipItemResponse from(Long equippedItemId, String name, ItemType type, boolean equipped) {
        return EquipItemResponse.builder()
                .equippedItemId(equippedItemId)
                .name(name)
                .type(type)
                .isEquipped(equipped)
                .build();
    }
}
