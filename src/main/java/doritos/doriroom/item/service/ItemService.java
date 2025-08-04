package doritos.doriroom.item.service;

import doritos.doriroom.item.domain.Item;
import doritos.doriroom.item.domain.ItemGroup;
import doritos.doriroom.item.domain.ItemType;
import doritos.doriroom.item.domain.UserItem;
import doritos.doriroom.item.dto.request.EquipItemRequest;
import doritos.doriroom.item.dto.request.PurchaseItemRequest;
import doritos.doriroom.item.dto.response.*;
import doritos.doriroom.item.exception.DuplicatedPurchasedItemException;
import doritos.doriroom.item.exception.ItemNotPurchasableException;
import doritos.doriroom.item.repository.ItemRepository;
import doritos.doriroom.item.repository.UserItemRepository;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import lombok.*;
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

    // 전체 아이템 조회 (유저 보유 여부 포함)
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems(User user) {
        Set<Long> ownedItemIds = userItemRepository.findByUser(user)
                .stream().map(ui -> ui.getItem().getItemId()).collect(Collectors.toSet());

        return itemRepository.findAll().stream()
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
                .stream().map(ui -> ui.getItem().getItemId()).collect(Collectors.toSet());;

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
        Long itemId = request.itemId();

        // 해당 아이템이 존재하는지 확인
        Item item = itemRepository.findById(itemId)
                .orElseThrow(ItemNotFoundException::new);

        // 구매 가능한 아이템인지 확인
        if (!item.isPurchasable()) {
            throw new ItemNotPurchasableException();
        }

        // 이미 구매한 아이템인지 확인
        if (userItemRepository.existsByUserAndItem(user, item)) {
            throw new DuplicatedPurchasedItemException();
        }

        user.deductCredit(item.getPrice()); // 크레딧 보유 여부 확인 및 차감

        // 아이템 저장
        UserItem userItem = UserItem.builder()
                .user(user)
                .item(item)
                .build();
        userItemRepository.save(userItem);

        return new PurchaseItemResponse(
                item.getItemId(),
                item.getName(),
                item.getPrice(),
                user.getCredit()
        );
    }

    // 아이템 착용 및 해제 (타입별)
    @Transactional
    public EquipItemResponse equip(User user, EquipItemRequest request) {
        Long itemId = request.itemId();

        // 해당 아이템이 존재하는지 확인
        Item item = itemRepository.findById(itemId)
                .orElseThrow(ItemNotFoundException::new);

        // 유저가 보유하고 있는지 확인
        UserItem userItem = userItemRepository.findByUserAndItem(user, item)
                .orElseThrow(NotOwnedException::new);

        // 해당 타입의 기존 착용 아이템 해제
        userItemRepository.findByUserAndItem_ItemTypeAndIsEquippedTrue(user, item.getItemType())
                .ifPresent(current -> {
                    current.unequip();
                    userItemRepository.save(current);
                });

        // 아이템 해제 및 착용
        if (userItem.isEquipped()) {
            userItem.unequip();
        } // 같은 아이템 착용 중이었다면 해제
        else {
            userItem.equip();
        }

        userItemRepository.save(userItem);

        return new EquipItemResponse(
                itemId,
                item.getName(),
                item.getItemType(),
                userItem.isEquipped()
        );
    }


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
