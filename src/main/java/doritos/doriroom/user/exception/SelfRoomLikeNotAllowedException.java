package doritos.doriroom.user.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class SelfRoomLikeNotAllowedException extends ApiException {
    public SelfRoomLikeNotAllowedException(){
        super(HttpStatus.BAD_REQUEST, "자신의 방은 좋아요 할 수 없습니다.");
    }
}
