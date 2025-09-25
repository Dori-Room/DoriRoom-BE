package doritos.doriroom.ranking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RankingSearchResponseDto(
    @Schema(description = "사용자 ID", example = "43d1a5a2-58dc-4786-8dba-27c62cae1943")
    UUID userId,
    
    @Schema(description = "사용자 닉네임", example = "도리토스")
    String nickname,
    
    @Schema(description = "프로필 사진 URL", example = "https://example.com/profile.jpg")
    String profileImageUrl,
    
    @Schema(description = "내가 이 유저를 팔로우하는지 여부", example = "true")
    boolean following,
    
    @Schema(description = "이 유저가 나를 팔로우하는지 여부", example = "false")
    boolean followedBy
) {
} 