package doritos.doriroom.tourApi.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class SigunguApiResponseDto {
    private SigunguInnerResponse response;

    @Data
    public static class SigunguInnerResponse {
        private SigunguHeader header;
        private SigunguBody body;
    }

    @Data
    public static class SigunguHeader {
        private String resultCode;
        private String resultMsg;
    }

    @Data
    public static class SigunguBody {
        private SigunguItems items;
        private long numOfRows;
        private int pageNo;
        private long totalCount;
    }

    @Data
    public static class SigunguItems {
        private List<SigunguApiItemDto> item;
    }
} 