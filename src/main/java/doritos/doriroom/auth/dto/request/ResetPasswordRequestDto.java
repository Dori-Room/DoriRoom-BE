package doritos.doriroom.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequestDto(
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @Schema(description = "이메일 주소", example = "user@example.com")
        @NotBlank String email,


//        @Pattern(regexp = "^[0-9]{6}$", message = "인증번호는 6자리 숫자여야 합니다.")
        @NotBlank String resetToken, // 사용자가 받은 임시토큰


        @Pattern(regexp = "^(?=.{8,20}$)((?=.*[a-zA-Z])(?=.*\\d)|(?=.*[a-zA-Z])(?=.*[!@#$%^&*])|(?=.*\\d)(?=.*[!@#$%^&*])).*$",
                message = "비밀번호는 영문, 숫자, 특수문자 중 2가지 이상을 조합하여 6~20자로 설정해야 합니다.")
        @Schema(description = "비밀번호(영문/숫자/특수문자 포함 8~20자)", example = "Passw0rd!")
        @NotBlank String newPassword
){}
