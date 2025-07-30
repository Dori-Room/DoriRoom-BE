package doritos.doriroom.item.repository;

import doritos.doriroom.item.domain.Item;
import doritos.doriroom.item.domain.ItemGroup;
import doritos.doriroom.item.domain.ItemType;
import doritos.doriroom.item.domain.UserItem;
import doritos.doriroom.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserItemRepository extends JpaRepository<UserItem, Long> {
    List<UserItem> findByUser(User user); // 유저가 보유한 전체 아이템 조회
    List<UserItem> findByUserAndItem_Group(User user, ItemGroup group); // 유저가 보유한 전체 그룹별 아이템 조회
    Optional<UserItem> findByUserAndItem(User user, Item item); // 유저가 보유한 아이템 단일 조회
    Optional<UserItem> findByUserAndItem_TypeAndIsEquippedTrue(User user, ItemType type); // 유저가 착용한 타입별 아이템 조회


    boolean existsByUserAndItem(User user, Item item); // 이미 보유하고 있는지 확인
}
