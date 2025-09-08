package doritos.doriroom.item.service;

import com.fasterxml.jackson.core.type.TypeReference;
import doritos.doriroom.global.cache.RedisCacheService;
import doritos.doriroom.item.domain.Item;
import doritos.doriroom.item.domain.ItemGroup;
import doritos.doriroom.item.domain.ItemType;
import doritos.doriroom.item.domain.UserItem;
import doritos.doriroom.item.dto.request.EquipItemRequest;
import doritos.doriroom.item.dto.request.PurchaseItemRequest;
import doritos.doriroom.item.dto.response.*;
import doritos.doriroom.item.exception.DuplicatedItemException;
import doritos.doriroom.item.exception.ItemNotPurchasableException;
import doritos.doriroom.item.repository.ItemRepository;
import doritos.doriroom.item.repository.UserItemRepository;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.exception.UserNotFoundException;
import doritos.doriroom.user.repository.UserRepository;

import java.util.Optional;
import java.util.UUID;
import lombok.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import doritos.doriroom.item.exception.ItemNotFoundException;
import doritos.doriroom.item.exception.NotOwnedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final UserItemRepository userItemRepository;
    private final UserRepository userRepository;
    private final RedisCacheService redisCacheService;

    /* ---- 사용자 아이템 추가 관련 (구매하는 경우 제외) ---- */

    @Transactional
    public void addToInventory(User user, Item item){ // 유효한 아이템 객체 가정
        // 이미 보유한 아이템인지 확인
        if (userItemRepository.existsByUserAndItem(user, item)) {
            throw new DuplicatedItemException("이미 보유하고 있는 아이템입니다.");
        }

        // 아이템을 인벤토리(유저 소유)에 저장
        UserItem userItem = UserItem.builder()
                .user(user)
                .item(item)
                .build();

        userItemRepository.save(userItem);
    }

    /* ---- 아이템 조회 관련 ---- */


    // 전체 아이템 조회 (유저 보유 여부 포함)
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems(User user) {
        // 캐시에서 아이템 조회
        Optional<List<Item>> cachedItems = redisCacheService.getCacheList(RedisCacheService.ALL_ITEMS_KEY, new TypeReference<>() {});

        List<Item> allItems;
        if (cachedItems.isPresent()) {
            allItems = cachedItems.get(); // 캐시 값이 있으면 할당하여 사용
        } else {
            allItems = itemRepository.findAll(); // 캐시에 없으면 db에서 조회
            redisCacheService.setCache(RedisCacheService.ALL_ITEMS_KEY, allItems, RedisCacheService.ALL_ITEM_TTL); // 캐시에 allItems 저장
        }

        // 유저의 보유 여부는 따로 조회
        Set<Long> ownedItemIds = userItemRepository.findByUser(user)
                .stream().map(ui -> ui.getItem().getItemId()).collect(Collectors.toSet());

        return allItems.stream()
                .map(i -> ItemResponse.from(i, ownedItemIds.contains(i.getItemId())))
                .toList();
    }

    // 유저 보유 아이템 전체 조회
    @Transactional(readOnly = true)
    public List<UserItemResponse> getUserItems(User user) {
        return userItemRepository.findByUser(user)
                .stream().map(UserItemResponse::from).toList();
    }

    // 전체 그룹별 아이템 조회 (유저 보유 여부 포함)
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItemsByGroup(User user, ItemGroup itemGroup, AreaGroup areaGroup) {
        validateGetItemsByGroupRequest(user, itemGroup, areaGroup); // 요청 유효성 검사

        // 전체 그룹별 아이템 조회
        List<Item> items = (itemGroup == ItemGroup.COMMON)
                ? itemRepository.findByItemGroup(ItemGroup.COMMON)
                : itemRepository.findByItemGroupAndAreaGroup(ItemGroup.AREA, areaGroup);

        // 유저가 소유한 itemId를 추출 (보유 여부 필드 값으로 사용)
        Set<Long> ownedItemIds = (itemGroup == ItemGroup.COMMON)
                ? userItemRepository.findByUserAndItem_ItemGroup(user, ItemGroup.COMMON)
                .stream().map(ui -> ui.getItem().getItemId()).collect(Collectors.toSet())
                : userItemRepository.findByUserAndItem_ItemGroupAndItem_AreaGroup(user, ItemGroup.AREA, areaGroup)
                .stream().map(ui -> ui.getItem().getItemId()).collect(Collectors.toSet());

        return items.stream()
                .map(item -> ItemResponse.from(item, ownedItemIds.contains(item.getItemId())))
                .collect(Collectors.toList());
    }

    // 유저 보유 그룹별 아이템 조회
    @Transactional(readOnly = true)
    public List<UserItemResponse> getUserItemsByGroup(User user, ItemGroup itemGroup, AreaGroup areaGroup) {
        validateGetItemsByGroupRequest(user, itemGroup, areaGroup); // 요청 유효성 검사

        return switch (itemGroup) {
            case COMMON -> userItemRepository.findByUserAndItem_ItemGroup(user, ItemGroup.COMMON)
                    .stream().map(UserItemResponse::from).toList();
            case AREA ->
                    userItemRepository.findByUserAndItem_ItemGroupAndItem_AreaGroup(user, ItemGroup.AREA, areaGroup)
                            .stream().map(UserItemResponse::from).toList();
        };
    }

    // 전체 타입별 아이템 조회 (유저 보유 여부 포함) (상점 UI)
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItemsByType(User user, ItemType itemType) {
        validateGetItemsByTypeRequest(user, itemType); // 요청 유효성 검사

        // 전체 타입별 아이템 조회
        List<Item> items = itemRepository.findByItemType(itemType);

        // 유저가 소유한 itemId를 추출 (보유 여부 필드 값으로 사용)
        Set<Long> ownedItemIds = userItemRepository.findByUserAndItem_ItemType(user, itemType)
                .stream().map(ui -> ui.getItem().getItemId()).collect(Collectors.toSet());

        return items.stream()
                .map(item -> ItemResponse.from(item, ownedItemIds.contains(item.getItemId())))
                .collect(Collectors.toList());
    }

    // 유저 보유 타입별 아이템 조회
    @Transactional(readOnly = true)
    public List<UserItemResponse> getUserItemsByType(User user, ItemType itemType) {
        validateGetItemsByTypeRequest(user, itemType); // 요청 유효성 검사

        return userItemRepository.findByUserAndItem_ItemType(user, itemType)
                .stream().map(UserItemResponse::from).toList();
    }


    // 단일 아이템 조회 (유저 보유 여부 포함)
    @Transactional(readOnly = true)
    public ItemResponse getItemDetails(User user, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(ItemNotFoundException::new);

        return ItemResponse.from(item, userItemRepository.existsByUserAndItem(user, item));
    }


    /* ---- 아이템 구매 관련 ---- */

    // 아이템 구매 확인 페이지 (구매 시 남은 크레딧 조회)
    @Transactional(readOnly = true)
    public PaymentViewResponse getPaymentInfo(User user, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(ItemNotFoundException::new);

        Long remainingCredit = user.getCredit() - item.getPrice();

        return PaymentViewResponse.from(item, remainingCredit);
    }

    // 아이템 구매
    @Transactional
    public PurchaseItemResponse purchase(User user, PurchaseItemRequest request) {
        // 해당 유저가 존재하는지 확인
        User foundUser = userRepository.findById(user.getUserId())
                .orElseThrow(UserNotFoundException::new);

        // 해당 아이템이 존재하는지 확인
        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(ItemNotFoundException::new);

        // 구매 가능한 아이템인지 확인
        if (!item.isPurchasable()) {
            throw new ItemNotPurchasableException();
        }

        // DB에서 조회한 foundUser의 크레딧 차감 로직 수행 (dirty checking)
        foundUser.deductCredit(item.getPrice()); // 크레딧 보유 여부 확인 및 차감

        // 아이템 저장
        addToInventory(foundUser, item);

        return new PurchaseItemResponse(
                item.getItemId(),
                item.getName(),
                item.getPrice(),
                foundUser.getCredit()
        );
    }


    /* ---- 아이템 착용 관련 ---- */

    // 아이템 착용 및 해제 (타입별)
    @Transactional
    public EquipItemResponse equip(User user, EquipItemRequest request) {
        // 해당 아이템이 존재하는지 확인
        Item item = itemRepository.findById(request.itemId())
                .orElseThrow(ItemNotFoundException::new);

        // 유저가 보유하고 있는지 확인
        UserItem userItem = userItemRepository.findByUserAndItem(user, item)
                .orElseThrow(NotOwnedException::new);

        // 해당 타입의 기존 착용 아이템이 있는지 여부 확인
        Optional<UserItem> currentlyEquippedItem = userItemRepository.findByUserAndItem_ItemTypeAndIsEquippedTrue(user, item.getItemType());

        // 유저 아이템의 착용 상태
        boolean isEquipped = userItem.isEquipped();

        // 해당 타입에 착용하고 있는 아이템이 있는 경우 이를 해제
        if (currentlyEquippedItem.isPresent()) {
            currentlyEquippedItem.get().unequip();
        }

        // 유저가 원래 착용 중이었던 아이템이 아니면 새로 착용
        if (!isEquipped) {
            userItem.equip();
        }

        userItemRepository.save(userItem);

        return new EquipItemResponse(
                item.getItemId(),
                item.getName(),
                item.getItemType(),
                userItem.isEquipped()
        );
    }

    // 현재 착용 중인 아이템 조회
    @Transactional(readOnly = true)
    public List<EquippedItemResponse> getEquippedItems(User user) {
        return userItemRepository.findByUserAndIsEquippedTrue(user)
                .stream().map(EquippedItemResponse::from).toList();
    }

    //다른 유저 착용 아이템 조회
    public List<EquippedItemResponse> getOtherUserEquippedItems(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        return userItemRepository.findByUserAndIsEquippedTrue(user)
                .stream().map(EquippedItemResponse::from).toList();
    }




    /* 내부 메서드 */

    // 요청 유효성 검사 메서드
    private void validateGetItemsByGroupRequest(User user, ItemGroup itemGroup, AreaGroup areaGroup) {
        if (user == null)
            throw new IllegalArgumentException("요청에 유저 정보가 없습니다.");

        if (itemGroup == null)
            throw new IllegalArgumentException("요청에 itemGroup 값이 필요합니다.");

        if (itemGroup == ItemGroup.AREA && areaGroup == null)
            throw new IllegalArgumentException("지역 아이템을 조회하려면 areaGroup 값이 필요합니다.");
    }
    private void validateGetItemsByTypeRequest(User user, ItemType itemType) {
        if (user == null)
            throw new IllegalArgumentException("요청에 유저 정보가 없습니다.");

        if (itemType == null)
            throw new IllegalArgumentException("요청에 itemType 값이 필요합니다.");

    }

}
