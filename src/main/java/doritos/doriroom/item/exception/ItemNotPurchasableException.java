package doritos.doriroom.item.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ItemNotPurchasableException extends ApiException {
    public ItemNotPurchasableException() {
        super(HttpStatus.BAD_REQUEST, "현재 구매 불가능한 아이템 입니다.");
    }
}
