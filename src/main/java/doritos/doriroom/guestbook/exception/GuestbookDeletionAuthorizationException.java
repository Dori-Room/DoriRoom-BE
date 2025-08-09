package doritos.doriroom.guestbook.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class GuestbookDeletionAuthorizationException extends ApiException {
    public GuestbookDeletionAuthorizationException() {
        super(HttpStatus.BAD_REQUEST, "방명록을 삭제할 권한이 없습니다.");
    }
}
