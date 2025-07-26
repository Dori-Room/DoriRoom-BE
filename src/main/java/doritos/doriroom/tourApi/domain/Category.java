package doritos.doriroom.tourApi.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {
    CULTURAL_TOURISM("EV010100", "문화관광축제"),
    CULTURAL_ARTS("EV010200", "문화예술축제"),
    LOCAL_SPECIALTY("EV010300", "지역특산물축제"),
    TRADITIONAL_HISTORY("EV010400", "전통역사축제"),
    ECO_NATURE("EV010500", "생태자연축제"),
    OTHER("EV010600", "기타축제"),
    PERFORMANCE("EV02", "공연"),
    FESTIVAL("EV03", "행사");

    private final String code;
    private final String name;
} 