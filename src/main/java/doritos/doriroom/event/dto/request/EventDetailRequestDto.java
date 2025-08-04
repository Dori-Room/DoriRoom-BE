package doritos.doriroom.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Schema(description = "축제 상세 조회 요청")
public record EventDetailRequestDto(
    @Schema(description = "축제 ID", example = "002cecce-05e2-4dbf-8994-6ed0922a8722")
    @NotNull(message = "축제 ID는 필수입니다")
    UUID eventId
) {}
