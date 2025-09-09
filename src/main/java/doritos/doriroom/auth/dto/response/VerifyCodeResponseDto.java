package doritos.doriroom.auth.dto.response;

public record VerifyCodeResponseDto(
        boolean verified,
        String resetToken,
        String username
) {
    public static VerifyCodeResponseDto of(boolean verified, String resetToken, String username){
        return new VerifyCodeResponseDto(verified, resetToken, username);
    }
}
