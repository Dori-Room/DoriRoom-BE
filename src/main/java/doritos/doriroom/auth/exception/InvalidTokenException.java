package doritos.doriroom.auth.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends ApiException {
    public InvalidTokenException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
