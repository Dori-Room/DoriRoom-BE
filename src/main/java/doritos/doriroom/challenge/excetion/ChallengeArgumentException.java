package doritos.doriroom.challenge.excetion;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ChallengeArgumentException extends ApiException {
    public ChallengeArgumentException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
