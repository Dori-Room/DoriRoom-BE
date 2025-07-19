package doritos.doriroom.user.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class DuplicateException extends ApiException {
    public DuplicateException(String field) {
        super(HttpStatus.CONFLICT, "이미 사용 중인 " + field + "입니다.");
    }
}