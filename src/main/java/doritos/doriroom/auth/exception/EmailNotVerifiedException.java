package doritos.doriroom.auth.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class EmailNotVerifiedException extends ApiException {
    public EmailNotVerifiedException() {
        super(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다.");
    }
}
