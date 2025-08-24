package doritos.doriroom.user.controller;


import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.dto.request.ChangePasswordRequestDto;
import doritos.doriroom.user.dto.request.UpdateProfileRequestDto;
import doritos.doriroom.user.dto.response.OtherUserRoomResponseDto;
import doritos.doriroom.user.dto.response.ProfileImageResponseDto;
import doritos.doriroom.user.dto.response.UserCreditResponseDto;
import doritos.doriroom.user.dto.response.UserMyPageInfoDetailResponseDto;
import doritos.doriroom.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
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

        @GetMapping("/view/{userId}")
        @Operation(summary = "다른 유저 방 정보 조회", description = "특정 유저의 방 정보 조회. 방문 시 조회수 증가")
        public ApiResponse<OtherUserRoomResponseDto> getOtherUserRoomInfo(
            @AuthenticationPrincipal User user,
            @RequestParam @Valid UUID userId
        ){
            return ApiResponse.ok(userService.getOhterUserRoomInfo(user.getUserId(), userId));
        }
}
