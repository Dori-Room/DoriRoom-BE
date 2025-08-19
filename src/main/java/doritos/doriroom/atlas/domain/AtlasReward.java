package doritos.doriroom.atlas.domain;

import doritos.doriroom.item.domain.Item;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "atlas_rewards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
@Builder
public class AtlasReward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atlas_id", nullable = false)
    private Atlas atlas; // 특정 지역 도감임을 지정

    @Column(nullable = false)
    private int targetLevel; // 해당 레벨 도달 시 아이템 지급

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item rewardItem; // 보상으로 지급할 아이템
}
