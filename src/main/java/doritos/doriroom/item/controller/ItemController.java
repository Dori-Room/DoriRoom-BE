package doritos.doriroom.item.controller;

import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.item.domain.ItemGroup;
import doritos.doriroom.item.dto.Request.EquipItemRequest;
import doritos.doriroom.item.dto.Request.ItemGroupRequest;
import doritos.doriroom.item.dto.Request.PurchaseItemRequest;
import doritos.doriroom.item.dto.Response.EquipItemResponse;
import doritos.doriroom.item.dto.Response.ItemResponse;
import doritos.doriroom.item.dto.Response.PurchaseItemResponse;
import doritos.doriroom.item.dto.Response.UserItemResponse;
import doritos.doriroom.item.service.ItemService;
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

    @GetMapping
    @Operation(summary = "전체 아이템 조회")
    public ApiResponse<List<ItemResponse>> getAllItems(@AuthenticationPrincipal User user){
        return ApiResponse.ok(itemService.getAllItems(user));
    }

    @GetMapping("/group")
    @Operation(summary = "그룹별(지역/일반과제) 아이템 조회")
    public ApiResponse<List<ItemResponse>> getAllItemsByGroup(@AuthenticationPrincipal User user, @RequestParam ItemGroup group) {
        return ApiResponse.ok(itemService.getAllItemsByGroup(user, group));
    }

    @GetMapping("/user")
    @Operation(summary = "유저 보유 아이템 조회")
    public ApiResponse<List<UserItemResponse>> getUserItems(@AuthenticationPrincipal User user){
        return ApiResponse.ok(itemService.getUserItems(user));
    }

    @GetMapping("/user/group")
    @Operation(summary = "그룹별(지역/일반과제) 유저 보유 아이템 조회")
    public ApiResponse<List<UserItemResponse>> getUserItemsByGroup(@AuthenticationPrincipal User user,  @RequestParam ItemGroup group){
        return ApiResponse.ok(itemService.getUserItemsByGroup(user, group));
    }

    @PostMapping("/purchase")
    @Operation(summary = "아이템 구매")
    public ApiResponse<PurchaseItemResponse> purchase(@AuthenticationPrincipal User user, @RequestBody @Valid PurchaseItemRequest request) {
        return ApiResponse.ok(itemService.purchase(user, request));
    }

    @PostMapping("/equip")
    @Operation(
            summary = "아이템 착용 및 해제",
            description = "요청한 아이템이 착용 중이면 해제, 착용 중이 아니면 착용 / 같은 타입의 아이템이 이미 착용되어 있을 경우 자동 해제"
    )
    public ApiResponse<EquipItemResponse> equip(@AuthenticationPrincipal User user, @RequestBody @Valid EquipItemRequest request) {
        return ApiResponse.ok(itemService.equip(user, request));
    }
}
