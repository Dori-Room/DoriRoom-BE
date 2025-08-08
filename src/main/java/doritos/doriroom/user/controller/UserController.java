package doritos.doriroom.user.controller;


import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

        @GetMapping("/check-username")
        @Operation(summary = "아이디 중복 확인")
        public ApiResponse<Void> checkUsername(@RequestParam String username){
            userService.checkUsernameDuplicate(username);
            return ApiResponse.ok();
        }

        @GetMapping("/check-nickname")
        @Operation(summary = "닉네임 중복 확인")
        public ApiResponse<Void> checkNickname(@RequestParam String nickname){
            userService.checkNicknameDuplicate(nickname);
            return ApiResponse.ok();
        }

//    @PostMapping("/signup")
//    @Operation(summary = "회원가입")
//    public ApiResponse<Void> signup(@RequestBody @Valid SignupRequestDto request){
//        userService.signup(request);
//        return ApiResponse.ok();
//    }
//
//    @PostMapping("/login")
//    @Operation(summary = "로그인")
//    public ApiResponse<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto request){
//        return ApiResponse.ok(userService.login(request));
//    }
//
//    @PostMapping("/reissue")
//    @Operation(summary = "access token 재발급")
//    public ApiResponse<TokenResponseDto> reissue(@RequestBody @Valid RefreshTokenRequestDto request){
//        return ApiResponse.ok(userService.reissue(request));
//    }
}
