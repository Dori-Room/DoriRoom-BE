package doritos.doriroom.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenRequestDto(
        @NotBlank(message = "FCM 토큰은 비어있을 수 없습니다.")
        String fcmToken
){}