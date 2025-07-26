package doritos.doriroom.tourApi.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class TourApiDetailInfoResponseDto {
    public TourApiDetailInfoInnerResponse response;

    @Data
    public static class TourApiDetailInfoInnerResponse {
        private TourApiHeader header;
        private TourApiDetailInfoBody body;
    }

    @Data
    public static class TourApiHeader {
        private String resultCode;
        private String resultMessage;
    }

    @Data
    public static class TourApiDetailInfoBody {
        private TourApiDetailInfoItems items;
        private long numOfRows;
        private int pageNo;
        private long totalCount;
    }

    @Data
    public static class TourApiDetailInfoItems {
        private List<TourApiDetailInfoDto> item;
    }
}
