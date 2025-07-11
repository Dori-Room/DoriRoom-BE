package doritos.doriroom.user.controller;


import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.user.dto.SignupRequestDto;
import doritos.doriroom.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    public ApiResponse<Void> signup(@RequestBody @Valid SignupRequestDto request){
        userService.signup(request);
        return ApiResponse.ok();
    }
}
