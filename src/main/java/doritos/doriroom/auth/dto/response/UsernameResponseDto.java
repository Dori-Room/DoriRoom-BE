package doritos.doriroom.auth.dto.response;


public record UsernameResponseDto(
        String username
) {
    public static UsernameResponseDto of(String username){
        return new UsernameResponseDto(username);
    }
}
