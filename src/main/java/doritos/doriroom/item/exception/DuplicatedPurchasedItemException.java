package doritos.doriroom.item.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class DuplicatedPurchasedItemException extends ApiException {
    public DuplicatedPurchasedItemException() {
        super(HttpStatus.CONFLICT, "이미 구매한 아이템 입니다.");
    }
}
