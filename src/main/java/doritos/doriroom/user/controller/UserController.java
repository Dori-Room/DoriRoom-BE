package doritos.doriroom.user.controller;


import doritos.doriroom.follow.dto.request.UserSearchRequestDto;
import doritos.doriroom.user.dto.response.UserSearchResultDto;
import doritos.doriroom.follow.service.FollowService;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.dto.request.ChangePasswordRequestDto;
import doritos.doriroom.user.dto.request.RoomLikeRequestDto;
import doritos.doriroom.user.dto.request.UpdateProfileRequestDto;
import doritos.doriroom.user.dto.response.MyRoomResponseDto;
import doritos.doriroom.user.dto.response.OtherUserRoomResponseDto;
import doritos.doriroom.user.dto.response.ProfileImageResponseDto;
import doritos.doriroom.user.dto.response.RoomLikeResponseDto;
import doritos.doriroom.user.dto.response.UserCreditResponseDto;
import doritos.doriroom.user.dto.response.UserMyPageInfoDetailResponseDto;
import doritos.doriroom.user.exception.UserNotFoundException;
import doritos.doriroom.user.service.RoomLikeService;
import doritos.doriroom.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User", description = "")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final RoomLikeService roomLikeService;
    private final FollowService followService;

        @GetMapping("/check-username")
        @Operation(summary = "아이디 중복 확인")
        public ApiResponse<Void> checkUsername(@RequestParam
                                               @Pattern(regexp = "^[a-z0-9]{4,12}$", message = "아이디는 영문 소문자 및 숫자 조합으로 4~12자만 사용할 수 있습니다.")
                                               String username){
            userService.checkUsernameDuplicate(username);
            return ApiResponse.ok();
        }

        @GetMapping("/check-nickname")
        @Operation(summary = "닉네임 중복 확인")
        public ApiResponse<Void> checkNickname(@RequestParam
                                               @Pattern(regexp = "^[가-힣a-zA-Z0-9]{2,10}$", message = "닉네임은 한글, 영문, 숫자 조합으로 1~10자만 가능합니다.")
                                               String nickname){
            userService.checkNicknameDuplicate(nickname);
            return ApiResponse.ok();
        }


        @GetMapping("/me")
        @Operation(summary = "내 정보 상세 조회")
        public ApiResponse<UserMyPageInfoDetailResponseDto> getUserInfoDetail(@AuthenticationPrincipal User user){
            return ApiResponse.ok(userService.getUserInfoDetail(user));
        }

        @GetMapping("/me/credit")
        @Operation(summary = "내 크레딧 조회")
        public ApiResponse<UserCreditResponseDto> getMyCredit(@AuthenticationPrincipal User user) {
            return ApiResponse.ok(userService.getUserCredit(user));
        }

        @PutMapping("/me/profile")
        @Operation(summary = "프로필 정보 수정 (닉네임)")
        public ApiResponse<Void> updateProfile(@AuthenticationPrincipal User user,
                                               @Valid @RequestBody UpdateProfileRequestDto request) {
            userService.updateProfile(user, request);
            return ApiResponse.ok();
        }

        @PostMapping("/me/profile-image")
        @Operation(summary = "프로필 이미지 업로드", description = """
            로그인한 사용자의 프로필 이미지를 업로드(또는 변경)합니다. `multipart/form-data` 형식으로 요청해야 합니다.
            
            **[제약 조건]**
            - 파일 형식: `jpg`, `jpeg`, `png`, `gif`, `bmp`, `webp`
            - 파일 크기: 최대 10MB
            """)
        public ApiResponse<ProfileImageResponseDto> uploadProfileImage(@AuthenticationPrincipal User user,
                                                                       @RequestParam("file") MultipartFile file) {
            return ApiResponse.ok(userService.uploadProfileImage(user, file));
        }

        @DeleteMapping("/me/profile-image")
        @Operation(summary = "프로필 이미지 삭제")
        public ApiResponse<Void> deleteProfileImage(@AuthenticationPrincipal User user) {
            userService.deleteProfileImage(user);
            return ApiResponse.ok();
        }

        @PutMapping("/me/password")
        @Operation(summary = "비밀번호 변경")
        public ApiResponse<Void> changePassword(@AuthenticationPrincipal User user,
                                                @Valid @RequestBody ChangePasswordRequestDto request) {
            userService.changePassword(user, request);
            return ApiResponse.ok();
        }

        @GetMapping("/room")
        @Operation(summary = "내 방 정보 조회", description = "현재 로그인한 사용자의 방 정보를 조회합니다.")
        public ApiResponse<MyRoomResponseDto> getMyRoomInfo(@AuthenticationPrincipal User user) {
            MyRoomResponseDto response = userService.getMyRoomInfo(user);
            return ApiResponse.ok(response);
        }

        @GetMapping("/room/{userId}")
        @Operation(summary = "다른 유저 방 정보 조회", description = "특정 유저의 방 정보 조회. 방문 시 조회수 증가")
        public ApiResponse<OtherUserRoomResponseDto> getOtherUserRoomInfo(
            @AuthenticationPrincipal User user,
            @RequestParam @Valid UUID userId
        ){
            return ApiResponse.ok(userService.getOtherUserRoomInfo(user.getUserId(), userId));
        }

    @Operation(summary = "방 좋아요 버튼", description = "방 좋아요를 추가 또는 취소합니다.")
    @PostMapping("/like")
    public ApiResponse<RoomLikeResponseDto> toggleLike(
        @AuthenticationPrincipal User user,
        @RequestBody RoomLikeRequestDto request
    ) {
        if (user == null) {
            throw new UserNotFoundException();
        }

        RoomLikeResponseDto response = roomLikeService.setLikeStatus(user, request.roomOwnerId(), request.isLiked());
        return ApiResponse.ok(response);
    }

    @Operation(summary = "방 좋아요 상태 확인", description = "특정 방의 좋아요 상태를 확인합니다.")
    @GetMapping("/like/check/{roomOwnerId}")
    public ApiResponse<Boolean> checkLike(
        @AuthenticationPrincipal User user,
        @Parameter(description = "방 주인 ID", example = "550e8400-e29b-41d4-a716-446655440001", required = true)
        @PathVariable("roomOwnerId") UUID roomOwnerId
    ) {
        if (user == null) {
            throw new UserNotFoundException();
        }

        boolean isLiked = roomLikeService.isLiked(user, roomOwnerId);
        return ApiResponse.ok(isLiked);
    }

    @Operation(summary = "사용자 검색", description = "닉네임으로 유저를 검색 (팔로우 상태를 함께 조회)")
    @PostMapping("/search")
    public ApiResponse<List<UserSearchResultDto>> searchUsers(@AuthenticationPrincipal User user,
                                                              @RequestBody UserSearchRequestDto request) {
        return ApiResponse.ok(userService.searchUsers(user, request));
    }
}
