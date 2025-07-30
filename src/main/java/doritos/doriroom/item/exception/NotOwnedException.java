package doritos.doriroom.item.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class NotOwnedException extends ApiException {
    public NotOwnedException() {
        super(HttpStatus.NOT_FOUND, "해당 아이템을 보유하고 있지 않습니다.");
    }
}
