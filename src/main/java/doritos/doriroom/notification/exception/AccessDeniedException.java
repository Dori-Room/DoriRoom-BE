package doritos.doriroom.notification.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class AccessDeniedException extends ApiException {
    public AccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "해당 알림을 읽을 권한이 없습니다.");
    }
}
