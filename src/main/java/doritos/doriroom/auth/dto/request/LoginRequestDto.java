package doritos.doriroom.auth.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;


public record LoginRequestDto (
        @Schema(description = "로그인 아이디", example = "user1234")
        @NotBlank String username,

        @Schema(description = "비밀번호", example = "Passw0rd!")
        @NotBlank String password
) {}
