package doritos.doriroom.ranking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RecentVisitResponseDto(
    @Schema(description = "사용자 ID", example = "43d1a5a2-58dc-4786-8dba-27c62cae1943")
    UUID userId,
    
    @Schema(description = "사용자 닉네임", example = "도리토스")
    String nickname,
    
    @Schema(description = "프로필 사진 URL", example = "https://example.com/profile.jpg")
    String profileImageUrl,

    @Schema(description = "방문 시간", example = "2025-01-15T14:30:00")
    String visitedAt
) {
} 