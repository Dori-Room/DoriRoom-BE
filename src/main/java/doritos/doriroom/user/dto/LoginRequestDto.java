package doritos.doriroom.user.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
public class LoginRequestDto {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
