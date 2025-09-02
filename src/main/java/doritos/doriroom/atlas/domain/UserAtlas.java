package doritos.doriroom.atlas.domain;

import doritos.doriroom.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "user_atlases",
        uniqueConstraints = { // 유저 당 지역별로 1개씩만 갖도록 유니크 제약조건 명시
                @UniqueConstraint(
                        name = "user_atlas_unique",
                        columnNames = {"user_id", "atlas_id"}
                )
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class UserAtlas { // 유저의 지역별 도감
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userAtlasId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atlas_id", nullable = false)
    private Atlas atlas;

    @Builder.Default @Column(nullable = false)
    private int level = 0;

    @Builder.Default @Column(nullable = false)
    private Long currentExp = 0L; // 현재 누적 경험치 (레벨 업 시 초기화 + 남은 레벨)
}
