package doritos.doriroom.event.dto.response;

import doritos.doriroom.diary.dto.response.DiaryResponseDto;
import doritos.doriroom.event.domain.Event;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

public record EventDiaryResponseDto(
    @Schema(description = "축제 ID", example = "002cecce-05e2-4dbf-8994-6ed0922a8722")
    UUID eventId,

    @Schema(description = "일기 목록")
    List<DiaryResponseDto> diaries
) {
    public static EventDiaryResponseDto from(Event event, List<DiaryResponseDto> diaries) {
        return new EventDiaryResponseDto(
            event.getEventId(),
            diaries
        );
    }
}
