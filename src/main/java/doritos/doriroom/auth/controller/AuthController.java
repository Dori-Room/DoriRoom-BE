package doritos.doriroom.auth.controller;


import doritos.doriroom.auth.dto.request.LoginRequestDto;
import doritos.doriroom.auth.dto.request.RefreshTokenRequestDto;
import doritos.doriroom.auth.dto.request.SignupRequestDto;
import doritos.doriroom.auth.dto.request.TokenResponseDto;
import doritos.doriroom.auth.dto.response.LoginResponseDto;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    //TODO: 이메일 주소 인증 (중복 확인)

    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    public ApiResponse<Void> signup(@RequestBody @Valid SignupRequestDto request){
        authService.signup(request);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    @Operation(summary = "로그인")
    public ApiResponse<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto request){
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/reissue")
    @Operation(summary = "access token 재발급")
    public ApiResponse<TokenResponseDto> reissue(@RequestBody @Valid RefreshTokenRequestDto request){
        return ApiResponse.ok(authService.reissue(request));
    }
}
