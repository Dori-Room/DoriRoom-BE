package doritos.doriroom.tourApi.domain;

import doritos.doriroom.tourApi.exception.AreaGroupNotFoundException;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AreaGroup {
    SEOUL(1, "서울", List.of(1)),
    GYEONGGI(2, "경기도", List.of(2, 31)),
    GANGWON(3, "강원도", List.of(32)),
    GYEONGSANG(4, "경상도", List.of(4, 6, 7, 35, 36)),
    JEOLLA(5, "전라도", List.of(5, 37, 38)),
    CHUNGNAM(6, "충청도", List.of(3, 8, 33, 34)),
    JEJU(7, "제주도", List.of(39));

    private final int code;
    private final String name;
    private final List<Integer> areaCodes;

    public static AreaGroup fromCode(int code) {
        for (AreaGroup group : values()) {
            if (group.getCode() == code) {
                return group;
            }
        }
        throw new AreaGroupNotFoundException();
    }
}
