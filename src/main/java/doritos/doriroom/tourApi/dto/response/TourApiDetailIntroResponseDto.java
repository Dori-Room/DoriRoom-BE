package doritos.doriroom.tourApi.dto.response;

import java.util.List;
import lombok.Data;

@Data
public class TourApiDetailIntroResponseDto {
    public TourApiDetailInnerResponse response;

    @Data
    public static class TourApiDetailInnerResponse {
        private TourApiHeader header;
        private TourApiDetailBody body;
    }

    @Data
    public static class TourApiHeader {
        private String resultCode;
        private String resultMessage;
    }

    @Data
    public static class TourApiDetailBody {
        private TourApiDetailItems items;
        private long numOfRows;
        private int pageNo;
        private long totalCount;
    }

    @Data
    public static class TourApiDetailItems {
        private List<TourApiDetailIntroDto> item;
    }
}
