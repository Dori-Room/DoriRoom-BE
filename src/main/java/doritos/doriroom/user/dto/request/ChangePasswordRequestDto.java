package doritos.doriroom.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequestDto(
        @NotBlank(message = "현재 비밀번호는 필수입니다")
        @Schema(description = "현재 비밀번호")
        String currentPassword,

        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Pattern(regexp = "^(?=.{6,20}$)((?=.*[a-zA-Z])(?=.*\\d)|(?=.*[a-zA-Z])(?=.*[!@#$%^&*])|(?=.*\\d)(?=.*[!@#$%^&*])).*$",
                message = "비밀번호는 영문, 숫자, 특수문자 중 2가지 이상을 조합하여 6~20자로 설정해야 합니다.")
        @Schema(description = "새 비밀번호(영문/숫자/특수문자 포함 8~20자)", example = "Passw0rd!2")
        String newPassword,

        @NotBlank(message = "비밀번호 확인은 필수입니다")
        @Schema(description = "새 비밀번호 확인")
        String confirmPassword
) {
    @AssertTrue(message = "새 비밀번호와 확인 비밀번호가 일치하지 않습니다")
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
