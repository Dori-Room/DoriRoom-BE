package doritos.doriroom.user.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
public class SignupRequestDto {

    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String nickname;
    // private String profileImageUrl;
}