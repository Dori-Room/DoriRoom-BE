package doritos.doriroom.user.dto.response;

import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;


@Builder
public record UserMyPageInfoResponseDto(
        @Schema(description = "사용자 ID", example = "43d1a5a2-58dc-4786-8dba-27c62cae1943")
        UUID userId,

        @Schema(description = "사용자 닉네임", example = "도리토스")
        String nickname,

        @Schema(description = "사용자 프로필 이미지")
        String profileImageUrl

) {
    public static UserMyPageInfoResponseDto from(User user) {
        if (user == null) {
            return null;
        }
        return UserMyPageInfoResponseDto.builder()
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}