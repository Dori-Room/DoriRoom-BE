package doritos.doriroom.follow.controller;

import doritos.doriroom.follow.dto.FollowFilterType;
import doritos.doriroom.follow.dto.request.*;
import doritos.doriroom.follow.dto.response.*;
import doritos.doriroom.follow.service.FollowService;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;


@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {
    private final FollowService followService;

    @Operation(summary = "팔로우하기", description = "특정 유저를 팔로우")
    @PostMapping
    public ApiResponse<FollowResponseDto> followUser(@AuthenticationPrincipal User user,
                                                     @RequestBody FollowRequestDto request){
        return ApiResponse.ok(followService.followUser(user, request));
    }

    @Operation(summary = "언팔로우하기", description = "특정 유저를 언팔로우")
    @DeleteMapping("/{targetUserId}")
    public ApiResponse<Void> unfollowUser(@AuthenticationPrincipal User user,
                                          @PathVariable UUID targetUserId){
        followService.unfollowUser(user, targetUserId);
        return ApiResponse.ok();
    }

    @Operation(summary = "단짝 친구 설정 및 해제", description = "팔로우한 유저를 단짝 친구로 설정하거나 해제. 변경하려는 상태를 요청에 포함하여 반영.")
    @PutMapping("/{targetUserId}/best-friend")
    public ApiResponse<FollowResponseDto> toggleBestFriend(@AuthenticationPrincipal User user,
                                                           @PathVariable UUID targetUserId,
                                                           @RequestBody SetBestFriendRequestDto request) {
        return ApiResponse.ok(followService.toggleBestFriend(user, targetUserId, request));
    }

    @Operation(summary = "팔로우 상태 확인", description = "특정 사용자와의 팔로우 관계 상태를 조회. 방에 방문 시 혹은 그 외에서 팔로우 버튼 선택 시 사용")
    @GetMapping("/status/{targetUserId}")
    public ApiResponse<FollowStatusResponseDto> getFollowStatus(@AuthenticationPrincipal User user,
                                                                @PathVariable UUID targetUserId) {
        return ApiResponse.ok(followService.getFollowStatus(user, targetUserId));
    }

    @Operation(summary = "단짝친구 여부 확인", description = "특정 유저와 단짝친구인지 여부만 확인")
    @GetMapping("/status/best/{targetUserId}")
    public ApiResponse<Boolean> checkBestFriendStatus(@AuthenticationPrincipal User user,
        @PathVariable UUID targetUserId) {
        return ApiResponse.ok(followService.isBestFriend(user, targetUserId));
    }


    @Operation(summary = "팔로워 목록 조회", description = "내가 팔로우하는 유저 목록을 조회. 최신, 오래된순, 단짝친구만(최신순) 으로 필터링. 기본값 최신순")
    @GetMapping("/followers")
    public ApiResponse<Page<FollowUserInfoDto>> getFollowingList(@AuthenticationPrincipal User user,
                                                                 @RequestParam(defaultValue = "RECENT") FollowFilterType filterType,
                                                                 @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(followService.getFollowerList(user, filterType, pageable));
    }

    @Operation(summary = "팔로잉 목록 조회", description = "나를 팔로우하는 유저 목록을 조회. 최신순, 오래된순으로 정렬")
    @GetMapping("/following")
    public ApiResponse<Page<FollowUserInfoDto>> getFollowerList(@AuthenticationPrincipal User user,
                                                                @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.ok(followService.getFollowingList(user, pageable));
    }

}