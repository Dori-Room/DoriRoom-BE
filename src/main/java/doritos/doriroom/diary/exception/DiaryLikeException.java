package doritos.doriroom.diary.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class DiaryLikeException extends ApiException {
    public DiaryLikeException() {
        super(HttpStatus.BAD_REQUEST, "일기 좋아요 처리 중 오류 발생");
    }
}
