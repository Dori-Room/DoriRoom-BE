package doritos.doriroom.diary.domain;

import doritos.doriroom.diary.dto.request.DiaryCreateRequestDto;
import doritos.doriroom.diary.dto.request.DiaryUpdateRequestDto;
import doritos.doriroom.user.domain.RoomVisibility;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "diary")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diary {
    @Id
    private UUID diaryId;

    @Column(nullable = false, length = 500)
    private String content;

    @ElementCollection
    @Column(name = "image_url")
    private List<String> imageUrls;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RoomVisibility diaryVisibility;

    @Column(nullable = false)
    private int likes;

    @Column(nullable = false)
    private LocalDate visitedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "event_id", nullable = false)
    private UUID eventId;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public static Diary from(UUID userId, DiaryCreateRequestDto request, List<String> imageUrls){
        return Diary.builder()
            .diaryId(UUID.randomUUID())
            .content(request.content())
            .imageUrls(imageUrls)
            .diaryVisibility(request.visibility())
            .likes(0)
            .visitedAt(request.visitedAt())
            .createdAt(LocalDateTime.now())
            .userId(userId)
            .eventId(request.eventId())
            .build();
    }

    public void updateDiary(DiaryUpdateRequestDto request) {
        if (request.visitedAt() != null) {
            this.visitedAt = request.visitedAt();
        }
        if (request.imageUrls() != null) {
            this.imageUrls = request.imageUrls();
        }
        if (request.content() != null) {
            this.content = request.content();
        }
        if (request.visibility() != null) {
            this.diaryVisibility = request.visibility();
        }
    }

    public void incrementLikes() {
        this.likes++;
    }

    public void decrementLikes() {
        if (this.likes > 0) {
            this.likes--;
        }
    }
}
