package doritos.doriroom.item.controller;

import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.item.domain.ItemGroup;
import doritos.doriroom.item.domain.ItemType;
import doritos.doriroom.item.dto.request.EquipItemRequest;
import doritos.doriroom.item.dto.request.PurchaseItemRequest;
import doritos.doriroom.item.dto.response.*;
import doritos.doriroom.item.service.ItemService;
import doritos.doriroom.tourApi.domain.AreaGroup;
import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    /* 전체 아이템 조회 */
    @GetMapping
    @Operation(summary = "전체 아이템 조회", description = "현재 로그인한 유저 기준으로, 모든 아이템을 조회 / 각 아이템의 보유 여부 반환함")
    public ApiResponse<List<ItemResponse>> getAllItems(@AuthenticationPrincipal User user){
        return ApiResponse.ok(itemService.getAllItems(user));
    }

    @GetMapping("/user")
    @Operation(summary = "유저 보유 아이템 조회", description = "아이템 착용 여부 반환함")
    public ApiResponse<List<UserItemResponse>> getUserItems(@AuthenticationPrincipal User user){
        return ApiResponse.ok(itemService.getUserItems(user));
    }

    /* 그룹별 아이템 조회 */
    @GetMapping("/group")
    @Operation(summary = "그룹별(지역/일반과제) 전체 아이템 조회", description = """
    아이템 소유 여부 포함
    그룹은 지역과 일반으로 나뉨
    - COMMON: 일반과제
    - AREA: 지역과제 (아래 AreaGroup 중 하나 필요)
      → SEOUL, GYEONGGI, CHUNGCHEONG, GANGWON, JEJU, GYEONGSANG, JEOLLA
    """)
    public ApiResponse<List<ItemResponse>> getAllItemsByGroup(@AuthenticationPrincipal User user, @RequestParam ItemGroup itemGroup,
                                                              @RequestParam(required = false) AreaGroup areaGroup) {
        return ApiResponse.ok(itemService.getAllItemsByGroup(user, itemGroup, areaGroup));
    }

    @GetMapping("/user/group")
    @Operation(summary = "그룹별(지역/일반과제) 유저 보유 아이템 조회", description = """
    착용 여부 포함
    - COMMON: 전체 일반과제 아이템 중 보유한 것
    - AREA + areaGroup: 특정 지역 아이템 중 보유한 것
    """)
    public ApiResponse<List<UserItemResponse>> getUserItemsByGroup(@AuthenticationPrincipal User user, @RequestParam ItemGroup itemGroup,
                                                                   @RequestParam(required = false) AreaGroup areaGroup){
        return ApiResponse.ok(itemService.getUserItemsByGroup(user, itemGroup, areaGroup));
    }

    /* 타입별 아이템 조회 */
    @GetMapping("/type")
    @Operation(summary = "타입별 전체 아이템 조회", description = """
    특정 아이템 타입(WALL, FLOOR, OBJECT, SHELF, WINDOW, APPAREL)에 해당하는 
    전체 아이템을 조회하며, 응답에는 사용자의 보유 여부도 함께 포함
    """
    )
    public ApiResponse<List<ItemResponse>> getAllItemsByType(@AuthenticationPrincipal User user, @RequestParam ItemType itemType) {
        return ApiResponse.ok(itemService.getAllItemsByType(user, itemType));
    }

    @GetMapping("/user/type")
    @Operation(summary = "타입별 유저 보유 아이템 조회", description = """
    특정 아이템 타입(WALL, FLOOR, OBJECT, SHELF, WINDOW, APPAREL)에 해당하는
    사용자가 보유 중인 아이템을 조회
    """
    )
    public ApiResponse<List<UserItemResponse>> getUserItemsByType(@AuthenticationPrincipal User user, @RequestParam ItemType itemType) {
        return ApiResponse.ok(itemService.getUserItemsByType(user, itemType));
    }


    @GetMapping("/{itemId}")
    @Operation(summary = "단일 아이템 상세 정보 조회, 사용자의 보유 여부 포함")
    public ApiResponse<ItemResponse> getItemDetails(@AuthenticationPrincipal User user, @PathVariable Long itemId) {
        return ApiResponse.ok(itemService.getItemDetails(user, itemId));
    }

    /* 아이템 구매 관련 */
    @GetMapping("/purchase/{itemId}")
    @Operation(summary = "아이템 구매 전 결제창 정보 조회")
    public ApiResponse<PaymentViewResponse> getPaymentInfo(@AuthenticationPrincipal User user, @PathVariable Long itemId) {
        return ApiResponse.ok(itemService.getPaymentInfo(user, itemId));
    }

    @PostMapping("/purchase")
    @Operation(summary = "아이템 구매")
    public ApiResponse<PurchaseItemResponse> purchase(@AuthenticationPrincipal User user, @RequestBody @Valid PurchaseItemRequest request) {
        return ApiResponse.ok(itemService.purchase(user, request));
    }

    /* 아이템 착용 관련 */
    @PostMapping("/equip")
    @Operation(summary = "아이템 착용 및 해제", description = "요청한 아이템이 착용 중이면 해제, 착용 중이 아니면 착용 / 같은 타입의 아이템이 이미 착용되어 있을 경우 자동 해제")
    public ApiResponse<EquipItemResponse> equip(@AuthenticationPrincipal User user, @RequestBody @Valid EquipItemRequest request) {
        return ApiResponse.ok(itemService.equip(user, request));
    }

    @GetMapping("/equip")
    @Operation(summary = "현재 착용 중인 아이템들 조회")
    public ApiResponse<List<EquippedItemResponse>> getEquippedItems(@AuthenticationPrincipal User user) {
        return ApiResponse.ok(itemService.getEquippedItems(user));
    }
}
