package doritos.doriroom.challenge.domain.challenge;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Enumerated(EnumType.STRING)
    private RewardType rewardType; // CREDIT, EXP, ITEM

    private Long amount; // CREDIT/EXP일 경우 사용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item rewardItem; // ITEM일 경우만 사용


}
