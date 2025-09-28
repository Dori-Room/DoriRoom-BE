package doritos.doriroom.item.repository;

import doritos.doriroom.item.domain.Item;
import doritos.doriroom.item.domain.ItemGroup;
import doritos.doriroom.item.domain.ItemType;
import doritos.doriroom.item.domain.UserItem;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {
    List<UserItem> findByUser(User user); // 유저가 보유한 전체 아이템 조회
    List<UserItem> findByUserAndItem_ItemGroup(User user, ItemGroup itemGroup); // 유저가 보유한 COMMON or AREA 아이템 조회
    List<UserItem> findByUserAndItem_ItemGroupAndItem_AreaGroup(User user, ItemGroup itemGroup, AreaGroup areaGroup); // 유저의 특정 지역별 아이템 조회
    List<UserItem> findByUserAndItem_ItemType(User user, ItemType itemType); // 유저가 보유한 타입별 아이템 조회

    Optional<UserItem> findByUserAndItem(User user, Item item); // 유저가 보유한 아이템 단일 조회
    Optional<UserItem> findByUserAndItem_ItemTypeAndEquippedTrue(User user, ItemType itemType); // 유저가 착용한 타입별 아이템 조회

    boolean existsByUserAndItem(User user, Item item); // 이미 보유하고 있는지 확인
    List<UserItem> findByUserAndEquippedTrue(User user); // 착용 중인 아이템들 조회

    @Query("""
    SELECT ui FROM UserItem ui
    JOIN FETCH ui.item i
    JOIN FETCH ui.user u
    WHERE u.userId IN :userIds
    AND ui.equipped = true
    """)
    List<UserItem> findByUser_UserIdInAndIsEquippedTrue(@Param("userIds") Set<UUID> userIds);
}
