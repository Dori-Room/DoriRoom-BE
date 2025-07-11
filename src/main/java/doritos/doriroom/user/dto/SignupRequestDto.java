package doritos.doriroom.user.dto;
import lombok.*;

@Getter
public class SignupRequestDto {
    private String username;
    private String password;
    private String nickname;
    // private String profileImageUrl;
}