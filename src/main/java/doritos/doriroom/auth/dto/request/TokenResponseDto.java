package doritos.doriroom.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class TokenResponseDto {
    private String accessToken;
    private String refreshToken;
}
