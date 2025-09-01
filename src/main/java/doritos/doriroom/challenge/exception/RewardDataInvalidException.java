package doritos.doriroom.challenge.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class RewardDataInvalidException extends ApiException {
    public RewardDataInvalidException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
