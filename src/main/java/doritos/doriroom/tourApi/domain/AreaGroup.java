package doritos.doriroom.tourApi.domain;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AreaGroup {
    SEOUL(1, "서울", List.of(1)),
    GYEONGGI(2, "경기", List.of(2, 31)),
    GANGWON(3, "강원", List.of(32)),
    GYEONGSANG(4, "경상", List.of(4, 6, 7, 35, 36)),
    JEOLLA(5, "전라", List.of(5, 37, 38)),
    CHUNGNAM(6, "충청", List.of(3, 8, 33, 34)),
    JEJU(7, "제주", List.of(39));

    private final int code;
    private final String name;
    private final List<Integer> areaCodes;

    public static AreaGroup fromCode(int code) {
        for (AreaGroup group : values()) {
            if (group.getCode() == code) {
                return group;
            }
        }
        throw new IllegalArgumentException("알 수 없는 지역 그룹 코드: " + code);
    }
}
