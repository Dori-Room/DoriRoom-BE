package doritos.doriroom.item.domain;

import doritos.doriroom.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "user_items", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "item_id"})
}) // 유저는 각 아이템을 하나씩만 소지 가능
public class UserItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @JoinColumn(name = "item_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Item item;

    @Column(name = "is_equipped", nullable = false)
    @Builder.Default
    private boolean equipped = false; // 착용 여부 상태

    // 아이템 착용/해제
    public void equip(){    this.equipped = true; }
    public void unequip(){  this.equipped = false; }

}
