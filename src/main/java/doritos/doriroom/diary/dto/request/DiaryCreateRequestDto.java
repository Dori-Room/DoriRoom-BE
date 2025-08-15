package doritos.doriroom.diary.dto.request;

import doritos.doriroom.user.domain.RoomVisibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "일기 생성 요청")
public record DiaryCreateRequestDto(
    @Schema(description = "축제 ID", example = "002cecce-05e2-4dbf-8994-6ed0922a8722")
    @NotNull(message = "축제 ID를 입력해주세요.")
    UUID eventId,

    @Schema(description = "방문 날짜", example = "2025-08-10")
    @NotNull(message = "방문 날짜를 입력해주세요.")
    @PastOrPresent(message = "방문 날짜는 과거 또는 현재 날짜여야 합니다.")
    LocalDate visitedAt,

    @Schema(
        description = "이미지 URL 목록 (선택사항, 최대 5개)",
        example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]",
        type = "array"
    )
    List<String> imageUrls,

    @Schema(description = "일기 내용", example = "오늘 축제에 다녀왔습니다. 정말 즐거웠어요!")
    @NotBlank(message = "내용을 입력해주세요.")
    @Size(max = 500, message = "일기 내용은 500자를 초과할 수 없습니다.")
    String content,

    @Schema(description = "방 공개 설정 PUBLIC / FOLLOW / PRIVATE", example = "PUBLIC")
    RoomVisibility visibility

) {
    public DiaryCreateRequestDto {
        if (imageUrls == null || imageUrls.isEmpty()) {
            imageUrls = List.of();
        } else {
            imageUrls = imageUrls.stream()
                .filter(url -> url != null && !url.trim().isEmpty())
                .distinct() // 중복 제거
                .limit(5)   // 최대 5개로 제한
                .toList();
        }
    }
} 