package doritos.doriroom.diary.dto.request;

import doritos.doriroom.user.domain.RoomVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

public record DiaryUpdateRequestDto(
    @Schema(description = "방문 날짜 (선택사항)", example = "2025-08-10")
    LocalDate visitedAt,

    @Schema(
        description = "이미지 URL 목록 (선택사항, 최대 5개)",
        example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]",
        type = "array"
    )
    List<String> imageUrls,

    @Schema(description = "일기 내용 (선택사항)", example = "오늘 축제에 다녀왔습니다. 정말 즐거웠어요!")
    @Size(max = 500, message = "일기 내용은 500자를 초과할 수 없습니다.")
    String content,

    @Schema(description = "방 공개 설정 (선택사항)", example = "PUBLIC")
    RoomVisibility visibility
) {
    public DiaryUpdateRequestDto {
        if (imageUrls == null) {
            imageUrls = List.of();
        }
        if (content == null) {
            content = "";
        }
    }
}
