package doritos.doriroom.auth.dto.response;

public record VerifyCodeResponseDto(
        boolean verified,
        String resetToken
) {
    public static VerifyCodeResponseDto of(boolean verified, String resetToken){
        return new VerifyCodeResponseDto(verified, resetToken);
    }
}
