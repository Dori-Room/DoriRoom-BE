package doritos.doriroom.item.domain;

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
    private ItemType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemGroup group;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    private CollectionTheme theme;

    @Column(nullable = false)
    private boolean isPurchasable; // true: 구매 가능, false: 이벤트/한정 지급
}
