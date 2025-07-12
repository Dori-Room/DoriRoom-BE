package doritos.doriroom.global.api.dto;

public class ApiResponse<T> {
    private final int statusCode;
    private final String message;
    private final T data;

    public static ApiResponse<Void> success(){
        return new ApiResponse<>(200, "요청 성공", null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "요청 성공", data);
    }

    public static <T> ApiResponse<T> error(int statusCode, String message){
        return new ApiResponse<>(statusCode, message, null);
    }
}
