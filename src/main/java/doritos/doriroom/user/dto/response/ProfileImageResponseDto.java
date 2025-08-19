package doritos.doriroom.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ProfileImageResponseDto(
        @Schema(description = "업로드된 프로필 이미지 URL")
        String profileImageUrl
) {
    public static ProfileImageResponseDto from(String profileImageUrl) {
        return ProfileImageResponseDto.builder()
                .profileImageUrl(profileImageUrl)
                .build();
    }
}