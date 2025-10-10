package doritos.doriroom.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import doritos.doriroom.notification.domain.Notification;
import doritos.doriroom.notification.domain.NotificationType;
import doritos.doriroom.notification.dto.NotificationRedirectDto;
import doritos.doriroom.notification.dto.NotificationResponseDto;
import doritos.doriroom.notification.exception.AccessDeniedException;
import doritos.doriroom.notification.exception.NotificationNotFoundException;
import doritos.doriroom.notification.repository.NotificationRepository;
import doritos.doriroom.user.domain.User;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;

    // 푸시 알림 전송
    @Transactional
    public void sendNotification(User user, NotificationType type, String placeholder, String targetId) {
        String content = type.createContent(placeholder); // content 구성

        // db에 알림 내용 저장
        Notification notification = Notification.builder()
                .user(user)
                .content(content)
                .type(type)
                .targetId(targetId)
                .build();
        notificationRepository.save(notification);

        // 푸시 알림 발송
        if (user.getFcmToken() != null && !user.getFcmToken().isBlank()) { // fcm 토큰이 있는지 검증
            // Firebase의 Notification 클래스 경로를 명시(이름 충돌 이슈)
            com.google.firebase.messaging.Notification fcmNotification =
                    com.google.firebase.messaging.Notification.builder()
                            .setTitle("도리룸") // 푸시 알림 제목
                            .setBody(content)     // 푸시 알림 내용
                            .build();

            Map<String, String> data = new HashMap<>();
            data.put("type", type.name());
            data.put("targetId", targetId);
            data.put("content", content);

            TransactionSynchronizationManager.registerSynchronization(new  TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    Message message = Message.builder()
                            .setNotification(fcmNotification)
                            .putAllData(data)
                            .setToken(user.getFcmToken())
                            .build();

                    try {
                        String response = FirebaseMessaging.getInstance().send(message);
                        log.info("FCM 푸시 알림 발송 성공: " + response);
                    } catch (FirebaseMessagingException e) {
                        log.error("FCM 푸시 알림 발송 실패", e);
                    }
                }
            });
        }
    }

    // 알림 목록 조회
    @Transactional
    public Page<NotificationResponseDto> getMyNotifications(User user, Pageable pageable) {
        Page<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable);

        notificationRepository.markAllAsReadByUser(user);  // 안 읽은 상태의 알림을 읽음 처리

        return notifications.map(NotificationResponseDto::from);
    }

    // 알림 읽음 처리
    @Transactional
    public NotificationRedirectDto readAndRedirect(User user, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(NotificationNotFoundException::new);

        if (!notification.getUser().getUserId().equals(user.getUserId())) { // 본인의 알림인지 확인
            throw new AccessDeniedException();
        }

        if (!notification.isRead()) {
            notification.markAsRead(); // 안 읽은 상태인 경우 -> 읽음 상태로 변경
        }

        String targetId = notification.getTargetId();
        if (targetId == null || targetId.isBlank()) {
            return NotificationRedirectDto.of("/");
        }

        String redirectUrl = switch (notification.getType()) {
            case DIARY_LIKE        -> "/api/diary/" + targetId; // diaryId
            case FOLLOWER      -> "/api/users/room/" + targetId; // userId
            case CHALLENGE_REWARD  -> "/api/challenges/" + targetId; // challengeId
            case GUESTBOOK_ENTRY -> "/api/guestbooks/room/" + targetId;  // roomOwnerId
            default -> "/";
        };

        return NotificationRedirectDto.of(redirectUrl);
    }
}
