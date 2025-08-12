package doritos.doriroom.auth.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidOrExpiredVerificationCodeException extends ApiException {
    public InvalidOrExpiredVerificationCodeException() {
        super(HttpStatus.BAD_REQUEST, "인증번호가 일치하지 않거나 만료되었습니다.");
    }
}
