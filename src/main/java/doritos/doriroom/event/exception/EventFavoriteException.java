package doritos.doriroom.event.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class EventFavoriteException extends ApiException {
    public EventFavoriteException() {
        super(HttpStatus.BAD_REQUEST, "축제 즐겨찾기 조회 중 오류 발생");
    }
} 