package doritos.doriroom.guestbook.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class SelfGuestbookNotAllowedException extends ApiException {
    public SelfGuestbookNotAllowedException() {
        super(HttpStatus.BAD_REQUEST, "자신의 방에는 방명록을 작성할 수 없습니다.");
    }
}
