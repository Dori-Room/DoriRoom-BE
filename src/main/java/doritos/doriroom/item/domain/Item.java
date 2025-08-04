package doritos.doriroom.item.domain;

import doritos.doriroom.tourApi.domain.AreaGroup;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType itemType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemGroup itemGroup; // COMMON | AREA

    @Enumerated(EnumType.STRING)
    private AreaGroup areaGroup; // itemGroup == ItemGroup.COMMON 이면 null

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    private CollectionTheme theme;

    @Column(nullable = false)
    private boolean isPurchasable; // true: 구매 가능, false: 이벤트/한정 지급
}
