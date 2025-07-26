package doritos.doriroom.tourApi.dto.response;

import lombok.Data;

@Data
public class TourApiDetailIntroDto {
    private String contentid;
    private String sponsor1;        // 주최사
    private String sponsor2;        // 주관사
    private String usetimefestival; // 이용시간
}
