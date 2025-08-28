package doritos.doriroom.atlas.domain;

import doritos.doriroom.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_atlas_rewards")
@Getter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserAtlasReward {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 유저가 받은 보상 아이템
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atlas_reward_id", nullable = false)
    private AtlasReward atlasReward;

    private LocalDateTime claimedAt; // 보상 수령 시각

    public static UserAtlasReward claimedReward(User user, AtlasReward atlasReward) {
        return UserAtlasReward.builder()
                .user(user)
                .atlasReward(atlasReward)
                .claimedAt(LocalDateTime.now()) // 생성 시점에 수령 시각 설정
                .build();
    }
}
