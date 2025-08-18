package doritos.doriroom.event.dto.response;

import doritos.doriroom.event.domain.Event;
import java.util.UUID;

public record EventInfoResponseDto(
    UUID eventId,
    String title
) {
    public static EventInfoResponseDto from(Event event){
        return new EventInfoResponseDto(
            event.getEventId(),
            event.getTitle()
        );
    }
}
