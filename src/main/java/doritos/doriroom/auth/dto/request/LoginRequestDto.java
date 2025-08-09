package doritos.doriroom.auth.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
public class LoginRequestDto {
    @NotBlank
    @Schema(description = "로그인 아이디", example = "user1234")
    private String username;
    @NotBlank
    @Schema(description = "비밀번호", example = "Passw0rd!")
    private String password;
}
