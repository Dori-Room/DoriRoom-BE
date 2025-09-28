package doritos.doriroom.ranking.dto.response;

import doritos.doriroom.item.dto.response.EquippedItemResponse;
import doritos.doriroom.tourApi.domain.AreaGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

import java.util.UUID;

@Builder
public record RegionalRankingResponseDto(
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
    
    @Schema(description = "도감 레벨", example = "15")
    int atlasLevel,
    
    @Schema(description = "도감 경험치", example = "2500")
    int atlasExp,
    
    @Schema(description = "지역 그룹", example = "SEOUL")
    AreaGroup areaGroup,
    
    @Schema(description = "내가 이 유저를 팔로우하는지 여부", example = "true")
    boolean following,
    
    @Schema(description = "이 유저가 나를 팔로우하는지 여부", example = "false")
    boolean followedBy
) {
} 