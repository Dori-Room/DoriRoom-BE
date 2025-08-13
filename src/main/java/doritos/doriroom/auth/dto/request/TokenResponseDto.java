package doritos.doriroom.auth.dto.request;


public record TokenResponseDto (
        String accessToken,
        String refreshToken
){}
