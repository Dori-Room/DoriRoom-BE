package doritos.doriroom.tourApi.domain;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SigunguId implements Serializable {
    // 시군구 아이디는 지역 아이디와 복합키로 설정
    private Integer areaCode;
    private Integer code;
}
