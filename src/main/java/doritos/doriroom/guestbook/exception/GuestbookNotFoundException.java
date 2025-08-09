package doritos.doriroom.guestbook.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class GuestbookNotFoundException extends ApiException {
    public GuestbookNotFoundException() {
        super(HttpStatus.NOT_FOUND, "방명록을 찾을 수 없습니다.");
    }

}
