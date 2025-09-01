package doritos.doriroom.item.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class DuplicatedItemException extends ApiException {
    public DuplicatedItemException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
