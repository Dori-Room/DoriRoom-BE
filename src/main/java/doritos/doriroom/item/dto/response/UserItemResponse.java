package doritos.doriroom.item.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import doritos.doriroom.item.domain.*;
import doritos.doriroom.tourApi.domain.AreaGroup;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserItemResponse (
        Long itemId,
        String name,
        String imageUrl,
        ItemType itemType,
        ItemGroup itemGroup, // COMMON or AREA
        AreaGroup areaGroup, // nullable
        CollectionTheme theme, // nullable
        boolean isEquipped
){
    public static UserItemResponse from(UserItem userItem){
        Item i = userItem.getItem();
        return UserItemResponse.builder()
                .itemId(i.getItemId())
                .name(i.getName())
                .imageUrl(i.getImageUrl())
                .itemType(i.getItemType())
                .itemGroup(i.getItemGroup())
                .areaGroup(i.getAreaGroup())
                .theme(i.getTheme())
                .isEquipped(userItem.isEquipped())
                .build();
    }
}
