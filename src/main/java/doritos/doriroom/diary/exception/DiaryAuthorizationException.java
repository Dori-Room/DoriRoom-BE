package doritos.doriroom.diary.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class DiaryAuthorizationException extends ApiException {
    public DiaryAuthorizationException() {
        super(HttpStatus.BAD_REQUEST, "본인이 작성한 일기만 수정/삭제할 수 있습니다.");
    }
}
