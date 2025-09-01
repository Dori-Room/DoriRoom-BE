package doritos.doriroom.atlas.domain;

import doritos.doriroom.tourApi.domain.AreaGroup;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "atlases")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) @AllArgsConstructor
@Builder
public class Atlas { // 지역별 도감 엔티티
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // atlasId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private AreaGroup areaGroup; // 어느 지역의 도감인지

}
