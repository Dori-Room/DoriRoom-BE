package doritos.doriroom.item.repository;

import doritos.doriroom.item.domain.Item;
import doritos.doriroom.item.domain.ItemGroup;
import doritos.doriroom.tourApi.domain.AreaGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findById(Long itemId); // 아이템 단일 조회
    List<Item> findByItemGroup(ItemGroup itemGroup); // COMMON 아이템 조회 or AREA 아이템 전체 조회
    List<Item> findByItemGroupAndAreaGroup(ItemGroup itemGroup, AreaGroup areaGroup); // 특정 지역별 아이템 조회 (ex JEJU)
}
