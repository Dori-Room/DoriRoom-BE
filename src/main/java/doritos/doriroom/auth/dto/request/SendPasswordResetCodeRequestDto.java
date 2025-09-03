package doritos.doriroom.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendPasswordResetCodeRequestDto(
        @NotBlank String username,
        @Email @NotBlank String email
) {}