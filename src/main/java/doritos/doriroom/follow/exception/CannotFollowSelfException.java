package doritos.doriroom.follow.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class CannotFollowSelfException extends ApiException {
    public CannotFollowSelfException() {
        super(HttpStatus.BAD_REQUEST, "자신을 팔로우할 수 없습니다.");
    }
}
