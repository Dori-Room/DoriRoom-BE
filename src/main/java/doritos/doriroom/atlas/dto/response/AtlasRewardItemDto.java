package doritos.doriroom.atlas.dto.response;

import doritos.doriroom.atlas.domain.AtlasReward;
import lombok.Builder;



@Builder
public record AtlasRewardItemDto(
        // 지역 도감 특정 기준 달성 시 받을 수 있는 보상 아이템 정보

        int targetLevel, // 해당 보상을 받을 수 있는 목표 레벨
        String itemName,
        String itemImageUrl

) {
    public static AtlasRewardItemDto from(AtlasReward reward) { // AtlasReward 엔티티를 받아 DTO로 변환
        if (reward == null)     return null;

        return AtlasRewardItemDto.builder()
                .targetLevel(reward.getTargetLevel())
                .itemName(reward.getRewardItem().getName())
                .itemImageUrl(reward.getRewardItem().getImageUrl())
                .build();
    }
}
