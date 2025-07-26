package doritos.doriroom.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

public record EventItemFilterRequestDto(
    @Schema(description = "지역코드 (1: 서울, 2: 인천)", example = "1")
    Integer areaCode,

    @Schema(description = "시군구코드 리스트", example = "[1, 2, 3]")
    List<Integer> sigunguCodes,

    @Schema(description = "분야코드 리스트 (EV010100: 문화관광축제, EV010200: 문화예술축제, EV02: 공연, EV03: 행사)", example = "[EV010100, EV010200]")
    List<String> categoryCodes,

    @Schema(description = "시작일", example = "2025.01.01")
    @DateTimeFormat(pattern = "yyyy.MM.dd")
    LocalDate startDate,

    @Schema(description = "종료일", example = "2025.03.01")
    @DateTimeFormat(pattern = "yyyy.MM.dd")
    LocalDate endDate,

    @Schema(description = "검색 키워드", example = "바닷가")
    String keyword
    ) {
}
