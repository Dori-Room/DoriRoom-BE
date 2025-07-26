package doritos.doriroom.tourApi.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class AreaGroupNotFoundException extends ApiException {
    public AreaGroupNotFoundException() {
        super(HttpStatus.NOT_FOUND, "지역그룹(도별) 정보를 찾을 수 없습니다.");
    }
}