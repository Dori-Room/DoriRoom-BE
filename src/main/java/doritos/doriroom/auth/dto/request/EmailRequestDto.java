package doritos.doriroom.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailRequestDto (
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank String email
)
{}
