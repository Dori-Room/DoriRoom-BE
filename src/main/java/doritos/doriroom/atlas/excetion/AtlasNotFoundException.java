package doritos.doriroom.atlas.excetion;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class AtlasNotFoundException extends ApiException {
    public AtlasNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 지역의 Atlas를 찾을 수 없습니다.");
    }
    public AtlasNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
