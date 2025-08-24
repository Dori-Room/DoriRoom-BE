package doritos.doriroom.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "room_likes", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"liker_id", "room_owner_id"})
})
@Getter @NoArgsConstructor @AllArgsConstructor
@Builder
public class RoomLike {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "liker_id", nullable = false)
    private User liker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_owner_id", nullable = false)
    private User roomOwner;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static RoomLike from(User liker, User roomOwner) {
        return RoomLike.builder()
            .liker(liker)
            .roomOwner(roomOwner)
            .build();
    }
}
