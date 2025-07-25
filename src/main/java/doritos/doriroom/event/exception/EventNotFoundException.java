package doritos.doriroom.event.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class EventNotFoundException extends ApiException {
    public EventNotFoundException() {
        super(HttpStatus.NOT_FOUND, "축제를 찾을 수 없습니다.");
    }
}
