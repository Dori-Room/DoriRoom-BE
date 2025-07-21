package doritos.doriroom.event.dto.response;

import doritos.doriroom.event.domain.Event;
import java.time.format.DateTimeFormatter;

public record EventResponseDto(
    String title,
    String contentId,
    String startDate,
    String endDate,
    String addr1,
    String firstImage,
    String secondImage
) {
    public static EventResponseDto from(Event e) {
        return new EventResponseDto(
            e.getTitle(),
            e.getContentId(),
            e.getStartDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
            e.getEndDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
            e.getAddr1(),
            e.getFirstImage(),
            e.getSecondImage()
        );
    }
}
