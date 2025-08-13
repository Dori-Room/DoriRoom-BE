package doritos.doriroom.auth.controller;


import doritos.doriroom.auth.dto.request.*;
import doritos.doriroom.auth.dto.response.LoginResponseDto;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/email")
    @Operation(summary = "이메일 인증 번호 발송")
    public ApiResponse<Void> sendVerificationEmail(@RequestBody @Valid EmailRequestDto request){
        authService.sendVerificationEmail(request);
        return ApiResponse.ok();
    }

    @PostMapping("/email/verify")
    @Operation(summary = "이메일 인증 번호 확인")
    public ApiResponse<Void> verifyEmail(@RequestBody @Valid EmailVerificationRequestDto request){
        authService.verifyEmail(request);
        return ApiResponse.ok();
    }

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

    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public ApiResponse<Void> logout(){
        return ApiResponse.ok();
    }
}
