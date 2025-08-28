package doritos.doriroom.challenge.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ChallengeNotFoundException extends ApiException {
    public ChallengeNotFoundException() {
        super(HttpStatus.NOT_FOUND, "도전과제를 찾을 수 없습니다.");
    }
    public ChallengeNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}