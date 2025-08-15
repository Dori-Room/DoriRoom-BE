package doritos.doriroom.diary.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DailyDiaryListResponseDto(
    @Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    UUID userId,

    @Schema(description = "조회 날짜", example = "2025-08-10")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date,

    @Schema(description = "해당 날짜의 일기 목록")
    List<DiaryResponseDto> diaries

) {
    public static DailyDiaryListResponseDto from(UUID userId, LocalDate date, List<DiaryResponseDto> diaryList) {
        return new DailyDiaryListResponseDto(
            userId,
            date,
            diaryList
        );
    }
}
