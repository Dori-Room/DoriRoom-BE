package doritos.doriroom.notification.domain;


import doritos.doriroom.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content; // 알림 내용

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type; // 알림 타입

    @Column(length = 36)
    private String targetId; // 페이지 이동 Id, UUID | Long

    @Builder.Default
    @Column(name = "is_read", nullable = false)
    private boolean read = false; // 알림 읽음 여부

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 알림 읽음 처리 메서드
    public void markAsRead() {
        this.read = true;
    }
}
