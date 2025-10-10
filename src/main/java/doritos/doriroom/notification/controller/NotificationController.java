package doritos.doriroom.notification.controller;

import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.notification.domain.NotificationType;
import doritos.doriroom.notification.dto.NotificationResponseDto;
import doritos.doriroom.notification.service.NotificationService;
import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @Operation(summary = "내 알림 목록 조회")
    @GetMapping
    public ApiResponse<Page<NotificationResponseDto>> getMyNotifications(@AuthenticationPrincipal User user, Pageable pageable) {
        return ApiResponse.ok(notificationService.getMyNotifications(user, pageable));
    }

    @Operation(summary = "특정 알림 읽음 처리")
    @PostMapping("/{notificationId}/read")
    public ApiResponse<Void> readNotification(@AuthenticationPrincipal User user,
                                                                @Parameter(description = "처리할 알림의 ID")
                                                                @PathVariable Long notificationId) {
        notificationService.readNotification(user, notificationId);
        return ApiResponse.ok();
    }

    @Operation(summary = "알림 발송 테스트")
    @PostMapping("/test")
    public ApiResponse<Void> testNotification(@AuthenticationPrincipal User user){
        notificationService.sendNotification(user, NotificationType.TEST_MESSAGE, "", "");
        return ApiResponse.ok();
    }
}