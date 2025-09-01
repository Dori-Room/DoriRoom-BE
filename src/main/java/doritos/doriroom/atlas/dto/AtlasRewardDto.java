package doritos.doriroom.atlas.dto;

import doritos.doriroom.atlas.domain.AtlasReward;
import doritos.doriroom.item.domain.ItemType;
import lombok.Builder;

import java.util.Objects;


@Builder
public record AtlasRewardDto(
        // 지역 도감 특정 기준 달성 시 받을 수 있는 보상 아이템 정보
        // 유저가 받을 수 있는 지역도감 보상 리스트

//        Long itemId, // 아이템 고유 id
        Long atlasRewardId, // 보상 받기(버튼) 요청 시 사용
        int targetLevel, // 해당 보상을 받을 수 있는 목표 레벨

        // 아이템 정보
        String itemName,
        String itemImageUrl,
        ItemType itemType,

        boolean isClaimed // 이미 받았는지 여부

){
    public static AtlasRewardDto of(AtlasReward atlasReward, boolean isClaimed) {
        if (atlasReward == null) return null;

        var item = atlasReward.getRewardItem();
        Objects.requireNonNull(item, "AtlasReward.rewardItem must not be null");

        return AtlasRewardDto.builder()
                .atlasRewardId(atlasReward.getId())
                .targetLevel(atlasReward.getTargetLevel())
                .itemName(atlasReward.getRewardItem().getName())
                .itemImageUrl(atlasReward.getRewardItem().getImageUrl())
                .itemType(atlasReward.getRewardItem().getItemType())
                .isClaimed(isClaimed)
                .build();
    }
}
