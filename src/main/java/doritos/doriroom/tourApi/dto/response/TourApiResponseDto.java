package doritos.doriroom.tourApi.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class TourApiResponseDto {
    public TourApiInnerResponse response;

    @Data
    public static class TourApiInnerResponse{
        private TourApiHeader header;
        private TourApiBody body;
    }

    @Data
    public static class TourApiHeader{
        private String resultCode;
        private String resultMessage;
    }

    @Data
    public static class TourApiBody{
        private Items items;
        private long numOfRows;
        private int pageNo;
        private long totalCount;
    }

    @Data
    public static class Items{
        private List<TourApiItemDto> item;
    }
}