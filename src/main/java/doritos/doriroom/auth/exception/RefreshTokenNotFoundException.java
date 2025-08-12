package doritos.doriroom.auth.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class RefreshTokenNotFoundException extends ApiException {
    public RefreshTokenNotFoundException() {
        super(HttpStatus.NOT_FOUND, "존재하지 않는 리프레시 토큰입니다.");
    }
}
