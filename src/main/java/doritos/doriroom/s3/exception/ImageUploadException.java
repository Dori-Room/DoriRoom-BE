package doritos.doriroom.s3.exception;

import doritos.doriroom.global.exception.ApiException;
import org.springframework.http.HttpStatus;

public class ImageUploadException extends ApiException {
    public ImageUploadException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
