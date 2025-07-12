package doritos.doriroom.global.api.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse<T> {

    private final int statusCode;
    private final String error;
    private final T content;

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(200, null, null);
    }

    public static <T> ApiResponse<T> ok(T content) {
        return new ApiResponse<>(200, null, content);
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String error) {
        return new ApiResponse<>(status.value(), error, null);
    }
}
