package doritos.doriroom.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record UpdateProfileRequestDto(
        @NotBlank(message = "닉네임은 필수입니다")
        @Size(min = 2, max = 10, message = "닉네임은 한글, 영문, 숫자 조합으로 2~10자만 가능합니다.")
        @Schema(description = "닉네임", example = "도리토스2")
        String nickname
) {
}