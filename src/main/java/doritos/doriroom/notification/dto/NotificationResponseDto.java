package doritos.doriroom.notification.dto;

import doritos.doriroom.notification.domain.Notification;
import doritos.doriroom.notification.domain.NotificationType;
import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record NotificationResponseDto(
        Long notificationId,
        String content,
        NotificationType type,
        boolean isRead,
        String targetId,
        LocalDateTime createdAt
) {
    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .notificationId(notification.getId())
                .content(notification.getContent())
                .type(notification.getType())
                .isRead(notification.isRead())
                .targetId(notification.getTargetId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}