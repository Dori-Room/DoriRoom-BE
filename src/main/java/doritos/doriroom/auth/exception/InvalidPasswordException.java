package doritos.doriroom.auth.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends ApiException {
    public InvalidPasswordException() {
        super(HttpStatus.BAD_REQUEST, "잘못된 비밀번호 입니다.");
    }
}
