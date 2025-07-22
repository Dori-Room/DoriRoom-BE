package doritos.doriroom.event.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class EventApiResponseDto {
    public EventApiInnerResponse response;

    @Data
    public static class EventApiInnerResponse{
        private EventHeader header;
        private EventBody body;
    }

    @Data
    public static class EventHeader{
        private String resultCode;
        private String resultMessage;
    }

    @Data
    public static class EventBody{
        private Items items;
        private long numOfRows;
        private int pageNo;
        private long totalCount;
    }

    @Data
    public static class Items{
        private List<EventApiItemDto> item;
    }
}