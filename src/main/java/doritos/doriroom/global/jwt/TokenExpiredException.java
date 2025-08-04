package doritos.doriroom.global.jwt;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class TokenExpiredException extends ApiException {
    public TokenExpiredException() {
        super(HttpStatus.UNAUTHORIZED, "만료된 토큰 입니다.");
    }
}
