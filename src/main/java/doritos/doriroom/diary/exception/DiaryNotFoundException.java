package doritos.doriroom.diary.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class DiaryNotFoundException extends ApiException {
    public DiaryNotFoundException() {
        super(HttpStatus.NOT_FOUND, "일기를 찾을 수 없습니다.");
    }
}
