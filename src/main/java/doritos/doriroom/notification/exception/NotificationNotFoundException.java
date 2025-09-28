package doritos.doriroom.notification.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NotificationNotFoundException extends ApiException {
    public NotificationNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 알림을 찾을 수 없습니다.");
    }
}
