package doritos.doriroom.auth.dto.request;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
public class LoginRequestDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
