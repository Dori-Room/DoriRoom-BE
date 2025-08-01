package doritos.doriroom.item.dto.response;

import doritos.doriroom.item.domain.*;
import lombok.Builder;

@Builder
public record UserItemResponse (
        Long itemId,
        String name,
        String imageUrl,
        ItemType type,
        ItemGroup group,
        CollectionTheme theme, // nullable
        boolean isEquipped
){
    public static UserItemResponse from(UserItem userItem){
        Item i = userItem.getItem();
        return UserItemResponse.builder()
                .itemId(i.getItemId())
                .name(i.getName())
                .imageUrl(i.getImageUrl())
                .type(i.getType())
                .group(i.getGroup())
                .theme(i.getTheme())
                .isEquipped(userItem.isEquipped())
                .build();
    }
}
