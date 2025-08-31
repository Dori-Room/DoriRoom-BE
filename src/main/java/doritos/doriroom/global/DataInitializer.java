package doritos.doriroom.global;

import doritos.doriroom.atlas.domain.Atlas;
import doritos.doriroom.atlas.repository.AtlasRepository;
import doritos.doriroom.item.domain.CollectionTheme;
import doritos.doriroom.item.domain.Item;
import doritos.doriroom.item.domain.ItemGroup;
import doritos.doriroom.item.domain.ItemType;
import doritos.doriroom.item.repository.ItemRepository;
import doritos.doriroom.tourApi.domain.AreaGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {
    private final AtlasRepository atlasRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        // 애플리케이션 시작 시 Atlas 데이터가 없으면 초기 데이터를 생성
        if (atlasRepository.count() == 0) {
            createInitialAtlases();
        }

        if (itemRepository.count() == 0) {
            createInitialItems();
        }
        long itemCount = itemRepository.count();
        System.out.println("현재 아이템 개수: " + itemCount);

    }

    // 원본 지역 도감 데이터
    private void createInitialAtlases() {
        // AreaGroup Enum의 모든 값을 순회하며 Atlas 객체를 생성
        List<Atlas> initialAtlases = Arrays.stream(AreaGroup.values())
                .map(areaGroup -> Atlas.builder().areaGroup(areaGroup).build())
                .collect(Collectors.toList());

        atlasRepository.saveAll(initialAtlases);
        System.out.println(initialAtlases.size() + "개 지역 Atlas 초기 데이터가 생성되었습니다.");
    }

    // 아이템 초기 데이터
    private void createInitialItems() {
        // --- WALL (벽지) 타입 아이템 3개 ---
        Item wall1 = Item.builder().name("심플한 나무 벽지").imageUrl("wall_wood_simple.png").itemType(ItemType.WALL).itemGroup(ItemGroup.COMMON).price(100L).isPurchasable(true).build();
        Item wall2 = Item.builder().name("겨울 눈꽃 벽지").imageUrl("wall_winter_snow.png").itemType(ItemType.WALL).itemGroup(ItemGroup.COMMON).price(300L).theme(CollectionTheme.WINTER).isPurchasable(true).build();
        Item wall3 = Item.builder().name("제주 돌담 벽지").imageUrl("wall_jeju_stone.png").itemType(ItemType.WALL).itemGroup(ItemGroup.AREA).areaGroup(AreaGroup.JEJU).price(0L).isPurchasable(false).build();

        // --- FLOOR (바닥) 타입 아이템 3개 ---
        Item floor1 = Item.builder().name("기본 마루 바닥").imageUrl("floor_wood_default.png").itemType(ItemType.FLOOR).itemGroup(ItemGroup.COMMON).price(100L).isPurchasable(true).build();
        Item floor2 = Item.builder().name("새해맞이 멍석").imageUrl("floor_newyear_mat.png").itemType(ItemType.FLOOR).itemGroup(ItemGroup.COMMON).price(400L).theme(CollectionTheme.NEW_YEAR).isPurchasable(true).build();
        Item floor3 = Item.builder().name("강원도 흙길 바닥").imageUrl("floor_gangwon_dirt.png").itemType(ItemType.FLOOR).itemGroup(ItemGroup.AREA).areaGroup(AreaGroup.GANGWON).price(0L).isPurchasable(false).build();

        // --- OBJECT (오브젝트) 타입 아이템 3개 ---
        Item object1 = Item.builder().name("기본 도리 화분").imageUrl("object_dori_pot.png").itemType(ItemType.OBJECT).itemGroup(ItemGroup.COMMON).price(0L).isPurchasable(false).build();
        Item object2 = Item.builder().name("여름 해변의 파라솔").imageUrl("object_summer_parasol.png").itemType(ItemType.OBJECT).itemGroup(ItemGroup.COMMON).price(500L).theme(CollectionTheme.SUMMER).isPurchasable(true).build();
        Item object3 = Item.builder().name("경상도 사과 바구니").imageUrl("object_gyeongsang_apple.png").itemType(ItemType.OBJECT).itemGroup(ItemGroup.AREA).areaGroup(AreaGroup.GYEONGSANG).price(0L).isPurchasable(false).build();

        // --- SHELF (선반) 타입 아이템 3개 ---
        Item shelf1 = Item.builder().name("기본 나무 선반").imageUrl("shelf_wood_default.png").itemType(ItemType.SHELF).itemGroup(ItemGroup.COMMON).price(150L).isPurchasable(true).build();
        Item shelf2 = Item.builder().name("겨울 얼음 선반").imageUrl("shelf_winter_ice.png").itemType(ItemType.SHELF).itemGroup(ItemGroup.COMMON).price(350L).theme(CollectionTheme.WINTER).isPurchasable(true).build();
        Item shelf3 = Item.builder().name("전라도 한지 선반").imageUrl("shelf_jeolla_hanji.png").itemType(ItemType.SHELF).itemGroup(ItemGroup.AREA).areaGroup(AreaGroup.JEOLLA).price(0L).isPurchasable(false).build();

        // --- WINDOW (창문) 타입 아이템 3개 ---
        Item window1 = Item.builder().name("심플한 사각 창문").imageUrl("window_square_simple.png").itemType(ItemType.WINDOW).itemGroup(ItemGroup.COMMON).price(200L).isPurchasable(true).build();
        Item window2 = Item.builder().name("여름 대나무 창문").imageUrl("window_summer_bamboo.png").itemType(ItemType.WINDOW).itemGroup(ItemGroup.COMMON).price(450L).theme(CollectionTheme.SUMMER).isPurchasable(true).build();
        Item window3 = Item.builder().name("충청도 격자 창문").imageUrl("window_chungcheong_lattice.png").itemType(ItemType.WINDOW).itemGroup(ItemGroup.AREA).areaGroup(AreaGroup.CHUNGNAM).price(0L).isPurchasable(false).build();

        // --- APPAREL (의상) 타입 아이템 3개 ---
        Item apparel1 = Item.builder().name("기본 도리 모자").imageUrl("apparel_dori_hat.png").itemType(ItemType.APPAREL).itemGroup(ItemGroup.COMMON).price(100L).isPurchasable(true).build();
        Item apparel2 = Item.builder().name("새해맞이 복주머니").imageUrl("apparel_newyear_pouch.png").itemType(ItemType.APPAREL).itemGroup(ItemGroup.COMMON).price(400L).theme(CollectionTheme.NEW_YEAR).isPurchasable(true).build();
        Item apparel3 = Item.builder().name("서울 선비 갓").imageUrl("apparel_seoul_gat.png").itemType(ItemType.APPAREL).itemGroup(ItemGroup.AREA).areaGroup(AreaGroup.SEOUL).price(0L).isPurchasable(false).build();

        itemRepository.saveAll(List.of(wall1, wall2, wall3, floor1, floor2, floor3, object1, object2, object3, shelf1, shelf2, shelf3, window1, window2, window3, apparel1, apparel2, apparel3));
        System.out.println("아이템 초기 데이터 18개가 생성되었습니다.");
    }
}