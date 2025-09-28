package doritos.doriroom.ranking.dto.response;

import doritos.doriroom.item.dto.response.EquippedItemResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RankingResponseDto(
    @Schema(description = "등수", example = "1")
    String rank,
    
    @Schema(description = "사용자 ID", example = "43d1a5a2-58dc-4786-8dba-27c62cae1943")
    UUID userId,
    
    @Schema(description = "사용자 닉네임", example = "도리토스")
    String nickname,

    @Schema(description = "사용자 한줄소개", example = "제 방에 좋아요 눌러주세요")
    String speech,

    @Schema(description = "장착한 아이템 목록")
    List<EquippedItemResponse> equippedItems,
    
    @Schema(description = "방 좋아요 수", example = "150")
    int likeCount,
    
    @Schema(description = "내가 이 유저를 팔로우하는지 여부", example = "true")
    boolean following,
    
    @Schema(description = "이 유저가 나를 팔로우하는지 여부", example = "false")
    boolean followedBy
) {
} 