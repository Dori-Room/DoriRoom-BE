package doritos.doriroom.event.dto.response;

import doritos.doriroom.event.domain.Event;
import doritos.doriroom.tourApi.domain.AreaGroup;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record EventResponseDto(
    String title,
    int contentId,
    UUID eventId,
    String startDate,
    String endDate,
    String addr1,
    String areaName,
    int areaCode,
    String firstImage,
    String secondImage
) {
    public static EventResponseDto from(Event e) {
        return new EventResponseDto(
            e.getTitle(),
            e.getContentId(),
            e.getEventId(),
            e.getStartDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
            e.getEndDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
            e.getAddr1(),
            AreaGroup.getAreaNameByAreaCode(e.getAreaCode()),
            e.getAreaCode(),
            e.getFirstImage(),
            e.getSecondImage()
        );
    }
}
