package doritos.doriroom.diary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import java.util.UUID;

public record DiaryWritingStatusResponseDto(
    @Schema(description = "사용자 ID", example = "43d1a5a2-58dc-4786-8dba-27c62cae1943")
    UUID userId,

    @Schema(description = "조회 연도", example = "2025")
    int year,

    @Schema(description = "조회 월 (1-12)", example = "8")
    int month,

    @Schema(description = "일별 작성 여부 (키: 일, 값: 작성 여부)",
        example = "{\"1\": false, \"2\": true, \"3\": false, \"4\": true, \"5\": false}")
    Map<String, Boolean> dailyStatus
) {
    public static DiaryWritingStatusResponseDto from(UUID userId, int year, int month, Map<String, Boolean> dailyStatus) {
        return new DiaryWritingStatusResponseDto(
            userId,
            year,
            month,
            dailyStatus
        );
    }
}
