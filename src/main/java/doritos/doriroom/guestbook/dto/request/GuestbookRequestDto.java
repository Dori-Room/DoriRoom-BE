package doritos.doriroom.guestbook.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Schema(description = "방명록 생성 요청")
public record GuestbookRequestDto(
    @Schema(description = "방 주인 ID / 개발 편의를 위해 user12의 UUID를 넣어놨습니다.", example = "353ebc2c-5161-4220-93f4-de84a08f9a5c")
    @NotNull(message = "방 주인 ID를 입력해주세요.")
    UUID roomOwnerId,

    @Schema(description = "방명록 내용", example = "방이 너무 예쁘네요. 방문하고 갑니다!")
    @NotBlank(message = "방명록 내용을 입력해주세요.")
    @Size(max = 500, message = "방명록은 500자를 초과할 수 없습니다.")
    String content
) {
}
