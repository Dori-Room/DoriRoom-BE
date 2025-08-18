package doritos.doriroom.diary.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "일기 좋아요 요청")
public record DiaryLikeRequestDto(
    @Schema(description = "일기 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotNull(message = "일기 ID는 필수입니다")
    UUID diaryId,

    @Schema(description = "좋아요 상태 (true: 추가, false: 취소)", example = "true")
    @NotNull(message = "좋아요 상태는 필수입니다")
    Boolean isLiked
) {
}
