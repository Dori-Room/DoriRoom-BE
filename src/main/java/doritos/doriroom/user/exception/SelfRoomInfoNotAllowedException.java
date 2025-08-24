package doritos.doriroom.user.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class SelfRoomInfoNotAllowedException extends ApiException {
    public SelfRoomInfoNotAllowedException(){
        super(HttpStatus.BAD_REQUEST, "자신의 방 정보는 이 API로 조회할 수 없습니다.");
    }
}
