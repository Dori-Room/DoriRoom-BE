package doritos.doriroom.challenge.domain.challenge;

import doritos.doriroom.challenge.exception.RewardDataInvalidException;
import doritos.doriroom.item.domain.Item;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "challenge_rewards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
@Builder
public class ChallengeReward { // 도전과제에 대한 보상 크레딧, 경험치, 아이템 정의
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 20)
    private RewardType rewardType; // CREDIT, EXP, ITEM

    private Long amount; // CREDIT/EXP일 경우 사용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item rewardItem; // ITEM일 경우만 사용

    @PrePersist
    @PreUpdate
    private void validateReward() {
        if (rewardType == null) {
            throw new RewardDataInvalidException("rewardType은 null일 수 없습니다.");
        }
        switch (rewardType) {
            case ITEM -> {
                if (rewardItem == null) throw new RewardDataInvalidException("ITEM 보상에는 rewardItem이 필요합니다.");
                amount = null; // 상태 정규화
            }
            case CREDIT, EXP -> {
                if (amount == null || amount <= 0) throw new RewardDataInvalidException("CREDIT/EXP 보상에는 amount(>0)가 필요합니다.");
                rewardItem = null; // 상태 정규화
            }
        }
    }
}
