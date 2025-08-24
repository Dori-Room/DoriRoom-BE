package doritos.doriroom.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record RoomLikeResponseDto(
    @Schema(description = "좋아요 상태 (true: 좋아요됨, false: 좋아요 취소됨)", example = "true")
    boolean isLiked,

    @Schema(description = "방 좋아요 수", example = "15")
    int likeCount
){
}
