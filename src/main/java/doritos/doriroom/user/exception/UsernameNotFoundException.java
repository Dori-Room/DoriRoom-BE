package doritos.doriroom.user.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class UsernameNotFoundException extends ApiException {
    public UsernameNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 유저가 없습니다.");
    }
}