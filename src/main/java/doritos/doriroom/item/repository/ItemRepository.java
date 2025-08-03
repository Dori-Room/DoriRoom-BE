package doritos.doriroom.item.repository;

import doritos.doriroom.item.domain.Item;
import doritos.doriroom.item.domain.ItemGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findById(Long itemId);
    List<Item> findByItemGroup(ItemGroup itemGroup); //지역별 또는 일반과제 조회
}
