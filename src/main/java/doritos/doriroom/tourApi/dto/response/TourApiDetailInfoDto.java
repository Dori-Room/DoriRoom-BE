package doritos.doriroom.tourApi.dto.response;

import lombok.Data;

@Data
public class TourApiDetailInfoDto {
    private String contentid;
    private String infoname;    // "행사소개" or "행사내용"
    private String infotext;
}
