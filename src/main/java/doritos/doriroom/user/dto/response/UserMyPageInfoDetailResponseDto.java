package doritos.doriroom.user.dto.response;

import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
public record UserMyPageInfoDetailResponseDto(
        @Schema(description = "사용자 ID", example = "43d1a5a2-58dc-4786-8dba-27c62cae1943")
        UUID userId,

        @Schema(description = "사용자 프로필 이미지")
        String profileImageUrl,

        @Schema(description = "사용자 닉네임", example = "도리토스")
        String nickname,

        @Schema(description = "사용자 아이디", example = "user1234")
        String username,

        @Schema(description = "사용자 이메일", example = "user@example.com")
        String email

) {
    public static UserMyPageInfoDetailResponseDto from(User user) {
        if (user == null) {
            return null;
        }
        return UserMyPageInfoDetailResponseDto.builder()
                .userId(user.getUserId())
                .profileImageUrl(user.getProfileImageUrl())
                .nickname(user.getNickname())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}