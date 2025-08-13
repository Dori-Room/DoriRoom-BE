package doritos.doriroom.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class EmailVerificationRequestDto {
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @Schema(description = "이메일", example = "user@example.com")
    @NotBlank private String email;

    @Pattern(regexp = "^[0-9]{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
    @Schema(description = "인증 코드", example = "123456")
    @NotBlank private String verificationCode;
}
