package doritos.doriroom.user.dto.response;

import doritos.doriroom.user.domain.User;

public record UserCreditResponseDto (
        Long userCredit
) {
    public static UserCreditResponseDto from(User user) {
        return new UserCreditResponseDto(user.getCredit());
    }
}