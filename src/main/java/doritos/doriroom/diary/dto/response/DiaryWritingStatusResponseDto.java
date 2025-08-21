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

    @Schema(description = "일별 작성 상태 정보")
    Map<String, DailyStatusInfo> dailyStatus
) {
    public record DailyStatusInfo(
        @Schema(description = "일기 작성 여부", example = "true")
        boolean isWritten,

        @Schema(description = "대표 이미지 URL (일기가 없거나 이미지가 없는 경우 null)",
            example = "http://example.com", nullable = true)
        String imageUrl
    ) {

        public static DailyStatusInfo notWritten() {
            return new DailyStatusInfo(false, null);
        }

        public static DailyStatusInfo writtenWithoutImage() {
            return new DailyStatusInfo(true, null);
        }

        public static DailyStatusInfo writtenWithImage(String imageUrl) {
            return new DailyStatusInfo(true, imageUrl);
        }
    }
    public static DiaryWritingStatusResponseDto from(UUID userId, int year, int month, Map<String, DailyStatusInfo> dailyStatusMap) {
        return new DiaryWritingStatusResponseDto(
            userId,
            year,
            month,
            dailyStatusMap
        );
    }
}
