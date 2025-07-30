package doritos.doriroom.item.service;

import doritos.doriroom.item.domain.Item;
import doritos.doriroom.item.domain.ItemGroup;
import doritos.doriroom.item.domain.ItemType;
import doritos.doriroom.item.domain.UserItem;
import doritos.doriroom.item.dto.Request.EquipItemRequest;
import doritos.doriroom.item.dto.Request.PurchaseItemRequest;
import doritos.doriroom.item.dto.Response.EquipItemResponse;
import doritos.doriroom.item.dto.Response.ItemResponse;
import doritos.doriroom.item.dto.Response.PurchaseItemResponse;
import doritos.doriroom.item.dto.Response.UserItemResponse;
import doritos.doriroom.item.exception.DuplicatedPurchasedItemException;
import doritos.doriroom.item.exception.ItemNotPurchasableException;
import doritos.doriroom.item.repository.ItemRepository;
import doritos.doriroom.item.repository.UserItemRepository;
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

    // 전체 아이템 조회
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItems(User user) {
        Set<Long> ownedItemIds = userItemRepository.findByUser(user)
                .stream().map(ui-> ui.getItem().getItemId()).collect(Collectors.toSet());

        return itemRepository.findAll().stream()
                .map(i -> ItemResponse.from(i, ownedItemIds.contains(i.getItemId())))
                .toList();
    }

    // 전체 그룹별 아이템 조회
    @Transactional(readOnly = true)
    public List<ItemResponse> getAllItemsByGroup(User user, ItemGroup group) {
        Set<Long> ownedItemIds = userItemRepository.findByUserAndItem_Group(user, group)
                .stream().map(ui-> ui.getItem().getItemId()).collect(Collectors.toSet());

        return itemRepository.findByGroup(group).stream()
                .map(i -> ItemResponse.from(i, ownedItemIds.contains(i.getItemId())))
                .toList();
    }

    // 유저 보유 아이템 전체 조회
    @Transactional(readOnly = true)
    public List<UserItemResponse> getUserItems(User user) {
        return userItemRepository.findByUser(user)
                .stream().map(UserItemResponse::from).toList();
    }

    // 유저 보유 아이템 그룹별 조회
    @Transactional(readOnly = true)
    public List<UserItemResponse> getUserItemsByGroup(User user, ItemGroup group) {
        return userItemRepository.findByUserAndItem_Group(user, group)
                .stream().map(UserItemResponse::from).toList();
    }

    // 현재 착용 중인 아이템 조회


    // 아이템 구매
    @Transactional
    public PurchaseItemResponse purchase(User user, PurchaseItemRequest request){
        Long itemId = request.itemId();

        // 해당 아이템이 존재하는지 확인
        Item item = itemRepository.findById(itemId)
                .orElseThrow(ItemNotFoundException::new);

        // 구매 가능한 아이템인지 확인
        if (!item.isPurchasable()){
            throw new ItemNotPurchasableException();
        }

        // 이미 구매한 아이템인지 확인
        if (userItemRepository.existsByUserAndItem(user, item)){
            throw new DuplicatedPurchasedItemException();
        }

//        // credit 보유 여부 확인
//        if (user.getCredit() < item.getPrice()){
//            throw new NotEnoughCreditException();
//        }

        user.deductCredit(item.getPrice()); // 크레딧 차감

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
    public EquipItemResponse equip(User user, EquipItemRequest request){
        Long itemId = request.itemId();

        // 해당 아이템이 존재하는지 확인
        Item item = itemRepository.findById(itemId)
                .orElseThrow(ItemNotFoundException::new);

        // 유저가 보유하고 있는지 확인
        UserItem userItem = userItemRepository.findByUserAndItem(user, item)
                .orElseThrow(NotOwnedException::new);

        // 해당 타입의 기존 착용 아이템 해제
        userItemRepository.findByUserAndItem_TypeAndIsEquippedTrue(user, item.getType())
                .ifPresent(current-> {
                    current.unequip();
                    userItemRepository.save(current);
                });

        // 아이템 해제 및 착용
        if (userItem.isEquipped()){ userItem.unequip(); } // 같은 아이템 착용 중이었다면 해제
        else { userItem.equip(); }

        userItemRepository.save(userItem);

        return new EquipItemResponse(
                itemId,
                item.getName(),
                item.getType(),
                userItem.isEquipped()
        );
    }



}
