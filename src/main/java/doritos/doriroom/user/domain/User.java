package doritos.doriroom.user.domain;
import doritos.doriroom.user.exception.NotEnoughCreditException;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Builder
public class User {
    @Id
    private UUID userId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String nickname;

    private String profileImageUrl;

    @Builder.Default
    @Column(nullable = false)
    private Long credit = 10000L; //TODO: 개발 편의용 크레딧 추가

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomVisibility roomVisibility = RoomVisibility.PUBLIC;

    @Builder.Default
    @Column(nullable = false)
    private int likeCount = 0;

    @Builder.Default
    @Column(nullable = false)
    private int viewCount = 0;

    // 포인트 관련 메서드 추가
    public void addCredit(int credit) {
        this.credit += credit;
    }

    // 보유 크레딧 차감
    public void deductCredit(long price){
        if(this.credit < price){ throw new NotEnoughCreditException();};
        this.credit -= price;
    }

    // 좋아요 수 증가
    public void incrementLikeCount() {
        this.likeCount++;
    }

    // 좋아요 수 감소
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}
