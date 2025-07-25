package doritos.doriroom.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class ApiException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String message;
}
