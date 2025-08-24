package doritos.doriroom.user.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class RoomLikeException extends ApiException {
    public RoomLikeException(){
        super(HttpStatus.INTERNAL_SERVER_ERROR, "방 좋아요 처리 중 오류가 발생했습니다.");
    }
}
