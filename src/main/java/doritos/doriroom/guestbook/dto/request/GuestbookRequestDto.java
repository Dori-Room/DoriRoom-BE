package doritos.doriroom.guestbook.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "방명록 생성 요청")
public record GuestbookRequestDto(
    @Schema(description = "방 주인 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @NotNull(message = "방 주인 ID를 입력해주세요.")
    UUID roomOwnerId,

    @Schema(description = "방명록 내용", example = "도리룸 방명록 내용 작성칸입니다.")
    @NotBlank(message = "방명록 내용을 입력해주세요.")
    @Size(max = 500, message = "방명록은 500자를 초과할 수 없습니다.")
    String content
) {
}
