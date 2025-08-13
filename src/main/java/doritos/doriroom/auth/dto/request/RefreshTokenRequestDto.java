package doritos.doriroom.auth.dto.request;

import jakarta.validation.constraints.NotBlank;


public record RefreshTokenRequestDto (
        @NotBlank String refreshToken
){}
