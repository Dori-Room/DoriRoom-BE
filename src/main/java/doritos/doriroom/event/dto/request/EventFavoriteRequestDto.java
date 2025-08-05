package doritos.doriroom.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "축제 즐겨찾기 요청")
public record EventFavoriteRequestDto(
    @Schema(description = "축제 ID", example = "002cecce-05e2-4dbf-8994-6ed0922a8722")
    @NotNull(message = "축제 ID는 필수입니다")
    UUID eventId,

    @Schema(description = "즐겨찾기 상태 (true: 추가, false: 취소)", example = "true")
    @NotNull(message = "즐겨찾기 상태는 필수입니다")
    Boolean isFavorite
) {
}
