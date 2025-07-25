package doritos.doriroom.tourApi.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class SigunguNotFoundException extends ApiException {
    public SigunguNotFoundException() {
        super(HttpStatus.NOT_FOUND, "시군구별 정보를 찾을 수 없습니다.");
    }
}