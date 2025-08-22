package doritos.doriroom.challenge.dto;

import doritos.doriroom.challenge.domain.challenge.ChallengeReward;
import doritos.doriroom.challenge.domain.challenge.RewardType;
import lombok.Builder;

@Builder
public record ChallengeRewardDto (
        RewardType rewardType, // 보상 종류 (CREDIT, EXP, ITEM)
        Long amount, // 수량 (CREDIT | EXP)

        // ITEM일 경우
        Long itemId,
        String itemName,
        String itemImageUrl
){
    public static ChallengeRewardDto from(ChallengeReward reward) {
        // 보상이 ITEM 타입인 경우에 설정하도록 bool값 추가
        boolean isItemReward = reward.getRewardType() == RewardType.ITEM && reward.getRewardItem() != null;

        return ChallengeRewardDto.builder()
                .rewardType(reward.getRewardType())
                .amount(reward.getAmount())
                .itemId(isItemReward ? reward.getRewardItem().getItemId() : null)
                .itemName(isItemReward ? reward.getRewardItem().getName() : null)
                .itemImageUrl(isItemReward ? reward.getRewardItem().getImageUrl() : null)
                .build();
    }
}
