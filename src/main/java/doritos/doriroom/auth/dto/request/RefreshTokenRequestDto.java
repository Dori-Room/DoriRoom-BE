package doritos.doriroom.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RefreshTokenRequestDto {
    @NotBlank
    private String refreshToken;
}
