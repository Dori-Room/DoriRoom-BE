package doritos.doriroom.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "방 좋아요 요청")
public record RoomLikeRequestDto(
    @Schema(description = "방 주인 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    @NotNull(message = "방 주인 ID는 필수입니다")
    UUID roomOwnerId,

    @Schema(description = "좋아요 상태 (true: 추가, false: 취소)", example = "true")
    @NotNull(message = "좋아요 상태는 필수입니다")
    Boolean isLiked
) {
}
