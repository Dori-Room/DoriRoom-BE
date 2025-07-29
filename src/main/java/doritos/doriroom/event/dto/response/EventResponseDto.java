package doritos.doriroom.event.dto.response;

import doritos.doriroom.event.domain.Event;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.tourApi.domain.Category;
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
    String categoryName,
    String firstImage,
    String secondImage
) {
    public static EventResponseDto from(Event e) {
        String categoryCode = getCategoryCode(e.getLclsSystm2(), e.getLclsSystm3());

        return new EventResponseDto(
            e.getTitle(),
            e.getContentId(),
            e.getEventId(),
            e.getStartDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
            e.getEndDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
            e.getAddr1(),
            AreaGroup.getAreaNameByAreaCode(e.getAreaCode()),
            e.getAreaCode(),
            getCategoryName(categoryCode),
            e.getFirstImage(),
            e.getSecondImage()
        );
    }

    private static String getCategoryCode(String lclsSystm2, String lclsSystm3) {
        if(lclsSystm2.startsWith("EV01")) {
            return lclsSystm3;
        }
        return lclsSystm2;
    }

    private static String getCategoryName(String categoryCode) {
        for (Category category : Category.values()) {
            if (category.getCode().equals(categoryCode)) {
                return category.getName();
            }
        }
        return "기타";
    }


}
