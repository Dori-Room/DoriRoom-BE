package doritos.doriroom.guestbook.domain;

import doritos.doriroom.guestbook.dto.request.GuestbookRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "guest_book")
@Getter @NoArgsConstructor @AllArgsConstructor
@Builder
public class Guestbook {
    @Id
    private UUID guestbookId;

    @Column(length = 200, nullable = false)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "writer_id", nullable = false)
    private UUID writerId;

    @Column(name = "room_owner_id", nullable = false)
    private UUID roomOwnerId;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static Guestbook from(UUID writerId, GuestbookRequestDto request){
        return Guestbook.builder()
            .guestbookId(UUID.randomUUID())
            .content(request.content())
            .writerId(writerId)
            .roomOwnerId(request.roomOwnerId())
            .build();
    }
}
