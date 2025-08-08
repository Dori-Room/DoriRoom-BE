package doritos.doriroom.user.controller;


import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
        public ApiResponse<Void> checkUsername(@RequestParam
                                               @Pattern(regexp = "^[a-z0-9]{4,12}$", message = "아이디는 영문 소문자 및 숫자 조합으로 4~12자만 사용할 수 있습니다.")
                                               String username){
            userService.checkUsernameDuplicate(username);
            return ApiResponse.ok();
        }

        @GetMapping("/check-nickname")
        @Operation(summary = "닉네임 중복 확인")
        public ApiResponse<Void> checkNickname(@RequestParam
                                               @Pattern(regexp = "^[가-힣a-zA-Z0-9]{1,10}$", message = "닉네임은 한글, 영문, 숫자 조합으로 1~10자만 가능합니다.")
                                               String nickname){
            userService.checkNicknameDuplicate(nickname);
            return ApiResponse.ok();
        }

}
