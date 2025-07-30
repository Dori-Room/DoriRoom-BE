package doritos.doriroom.item.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ItemNotFoundException extends ApiException {
    public ItemNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 아이템을 찾을 수 없습니다.");
    }
}
