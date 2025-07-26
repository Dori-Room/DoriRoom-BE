package doritos.doriroom.event.dto.response;

import doritos.doriroom.event.domain.Event;
import doritos.doriroom.tourApi.domain.AreaGroup;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record EventDetailResponseDto(
    String title,
    int contentId,
    UUID eventId,
    String startDate,
    String endDate,
    String addr1,
    String areaName,
    int areaCode,
    String firstImage,
    String secondImage,

    //상세정보
    String sponsor1,
    String sponsor2,
    String useTimeFestival,

    String eventIntro,
    String eventContent,

    //위치정보
    Double mapX,
    Double mapY
) {
    public static EventDetailResponseDto from(Event event){
        return new EventDetailResponseDto(
            event.getTitle(),
            event.getContentId(),
            event.getEventId(),
            event.getStartDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
            event.getEndDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
            event.getAddr1(),
            AreaGroup.getAreaNameByAreaCode(event.getAreaCode()),
            event.getAreaCode(),
            event.getFirstImage(),
            event.getSecondImage(),
            event.getSponsor1(),
            event.getSponsor2(),
            event.getUseTimeFestival(),
            event.getEventIntro(),
            event.getEventContent(),
            event.getMapX(),
            event.getMapY()
        );
    }
}
