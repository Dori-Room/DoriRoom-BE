package doritos.doriroom.challenge.domain.userchallenge;

import doritos.doriroom.challenge.domain.challenge.Challenge;
import doritos.doriroom.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_challenges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChallenge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Enumerated(EnumType.STRING)
    private ChallengeStatus status; // 유저의 과제 진행 상태 / NOT_STARTED, IN_PROGRESS, WAIT_REWARD, COMPLETED

    @Builder.Default @Column(nullable = false)
    private int currentProgress = 0; // 유저의 과제에 대한 진행도 계산. n회성 과제인 경우 수행 후 +n 하면 종료 상태 처리함

}
