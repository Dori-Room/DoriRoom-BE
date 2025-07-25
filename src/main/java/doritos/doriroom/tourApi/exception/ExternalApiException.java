package doritos.doriroom.tourApi.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ExternalApiException extends ApiException {
    public ExternalApiException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
