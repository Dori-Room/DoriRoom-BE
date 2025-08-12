package doritos.doriroom.auth.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class EmailSendFailedException extends ApiException {
    public EmailSendFailedException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다.");
    }
}
