package doritos.doriroom.atlas.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class InsufficientAtlasLevelException extends ApiException {
    public InsufficientAtlasLevelException() {
        super(HttpStatus.FORBIDDEN, "보상을 받을 수 있는 레벨에 도달하지 못했습니다.");
    }
}
