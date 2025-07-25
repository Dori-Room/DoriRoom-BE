package doritos.doriroom.tourApi.exception;


import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class AreaNotFoundException extends ApiException {
    public AreaNotFoundException() {
        super(HttpStatus.NOT_FOUND, "지역 정보를 찾을 수 없습니다.");
    }
}