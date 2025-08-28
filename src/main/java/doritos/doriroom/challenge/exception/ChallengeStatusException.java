package doritos.doriroom.challenge.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ChallengeStatusException extends ApiException {
    public ChallengeStatusException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}