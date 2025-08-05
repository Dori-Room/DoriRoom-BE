package doritos.doriroom.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Schema(description = "축제 즐겨찾기 삭제 요청 DTO")
public record EventFavoriteDeleteRequestDto(
    @Schema(description = "삭제할 축제 ID 목록", example = "[\"002cecce-05e2-4dbf-8994-6ed0922a8722\", \"123e4567-e89b-12d3-a456-426614174000\"]")
    @NotEmpty(message = "삭제할 축제 ID 목록은 필수입니다")
    @Size(max = 100, message = "한 번에 삭제할 수 있는 축제는 최대 100개입니다")
    List<UUID> eventIds
) {
}
