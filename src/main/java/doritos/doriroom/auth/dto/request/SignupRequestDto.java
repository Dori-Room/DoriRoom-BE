package doritos.doriroom.auth.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public record SignupRequestDto (
        @Pattern(regexp = "^[a-z0-9]{4,12}$",
                message = "아이디는 영문 소문자 및 숫자 조합으로 4~12자만 사용할 수 있습니다.")
        @Schema(description = "로그인 아이디(소문자+숫자, 4~20자)", example = "user1234")
        @NotBlank String username,

        @Pattern(regexp = "^(?=.{6,20}$)((?=.*[a-zA-Z])(?=.*\\d)|(?=.*[a-zA-Z])(?=.*[!@#$%^&*])|(?=.*\\d)(?=.*[!@#$%^&*])).*$",
                message = "비밀번호는 영문, 숫자, 특수문자 중 2가지 이상을 조합하여 6~20자로 설정해야 합니다.")
        @Schema(description = "비밀번호(영문/숫자/특수문자 포함 8~20자)", example = "Passw0rd!")
        @NotBlank String password,

        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @Schema(description = "이메일 주소", example = "user@example.com")
        @NotBlank String email,

        @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$",
                message = "닉네임은 한글, 영문, 숫자 조합으로 2~10자만 가능합니다.")
        @Schema(description = "닉네임(한글/영문/숫자/_, 2~16자)", example = "홍길동")
        @NotBlank String nickname
){}