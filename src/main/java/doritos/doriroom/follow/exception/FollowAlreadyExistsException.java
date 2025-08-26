package doritos.doriroom.follow.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class FollowAlreadyExistsException extends ApiException {
    public FollowAlreadyExistsException() {
        super(HttpStatus.CONFLICT, "이미 팔로우 중인 사용자입니다.");

    }
}