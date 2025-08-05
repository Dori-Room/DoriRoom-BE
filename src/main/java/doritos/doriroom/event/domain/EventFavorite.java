package doritos.doriroom.event.domain;

import doritos.doriroom.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "event_favorite")
@Getter @NoArgsConstructor @AllArgsConstructor
@Builder
public class EventFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // 복합 유니크 제약조건: 한 사용자가 같은 축제를 중복 즐겨찾기할 수 없음
    @Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "event_id"})
    })
    public static class EventFavoriteId {
    }
}