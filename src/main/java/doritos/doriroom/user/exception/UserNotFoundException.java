package doritos.doriroom.user.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 유저가 없습니다.");
    }
    public UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}