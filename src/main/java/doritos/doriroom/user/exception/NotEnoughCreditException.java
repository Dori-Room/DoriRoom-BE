package doritos.doriroom.user.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NotEnoughCreditException extends ApiException {
    public NotEnoughCreditException() {
        super(HttpStatus.BAD_REQUEST, "보유 크레딧이 충분하지 않습니다.");
    }
    public NotEnoughCreditException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

}
