package doritos.doriroom.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class LoginResponseDto {
    private String accessToken;
    private String refreshToken;
}
