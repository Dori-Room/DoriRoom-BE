package doritos.doriroom.diary.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class DuplicateDiaryException extends ApiException {
    public DuplicateDiaryException(){
        super(HttpStatus.BAD_REQUEST, "이미 해당 축제에 일기를 작성했습니다.");
    }
}
