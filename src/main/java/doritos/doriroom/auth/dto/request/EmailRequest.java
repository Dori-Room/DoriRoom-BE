package doritos.doriroom.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class EmailRequest {
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    @NotBlank private String email;
}
