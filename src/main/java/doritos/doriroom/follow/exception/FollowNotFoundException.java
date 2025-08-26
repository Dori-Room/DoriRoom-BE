package doritos.doriroom.follow.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class FollowNotFoundException extends ApiException {
    public FollowNotFoundException() {
        super(HttpStatus.NOT_FOUND, "팔로우 관계가 존재하지 않습니다.");

    }
}