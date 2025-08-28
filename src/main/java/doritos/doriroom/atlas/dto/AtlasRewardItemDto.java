package doritos.doriroom.atlas.dto.response;

import doritos.doriroom.atlas.domain.AtlasReward;
import doritos.doriroom.item.domain.CollectionTheme;
import doritos.doriroom.item.domain.ItemType;
import doritos.doriroom.tourApi.domain.AreaGroup;
import lombok.Builder;



@Builder
public record AtlasRewardItemDto(
        // 지역 도감 특정 기준 달성 시 받을 수 있는 보상 아이템 정보


        int targetLevel, // 해당 보상을 받을 수 있는 목표 레벨
        Long itemId, // 아이템 고유 id
        String itemName,
        String itemImageUrl,
        ItemType itemType,
        AreaGroup areaGroup,
        CollectionTheme theme

) {
    public static AtlasRewardItemDto from(AtlasReward reward) { // AtlasReward 엔티티를 받아 DTO로 변환
        if (reward == null)     return null;

        return AtlasRewardItemDto.builder()
                .targetLevel(reward.getTargetLevel())
                .itemId(reward.getRewardItem().getItemId())
                .itemName(reward.getRewardItem().getName())
                .itemImageUrl(reward.getRewardItem().getImageUrl())
                .itemType(reward.getRewardItem().getItemType())
                .areaGroup(reward.getRewardItem().getAreaGroup())
                .theme(reward.getRewardItem().getTheme())
                .build();
    }
}
