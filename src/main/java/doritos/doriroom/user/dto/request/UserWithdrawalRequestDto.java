package doritos.doriroom.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UserWithdrawalRequestDto(
        @NotBlank(message = "현재 비밀번호를 입력해주세요.")
        String password
) {}