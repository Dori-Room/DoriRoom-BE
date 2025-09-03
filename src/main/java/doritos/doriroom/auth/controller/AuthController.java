package doritos.doriroom.auth.controller;


import doritos.doriroom.auth.dto.request.*;
import doritos.doriroom.auth.dto.response.LoginResponseDto;
import doritos.doriroom.global.dto.ApiResponse;
import doritos.doriroom.auth.service.AuthService;
import doritos.doriroom.global.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

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
    public ApiResponse<Void> signup(@RequestPart("request") @Valid SignupRequestDto request,
                                    @RequestPart(value = "image", required = false) MultipartFile image){
        authService.signup(request, image);
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
    @Operation(summary = "로그아웃", description = """
        사용자의 세션을 종료
        - 저장된 리프레시 토큰 삭제
        - 요청에 사용된 액세스 토큰 블랙리스트 처리
        클라이언트는 로그아웃 이후 로컬의 액세스/리프레시 토큰을 삭제해야 합니다.
        """)
    @SecurityRequirement(name = "bearerAuth")
    public ApiResponse<Void> logout(HttpServletRequest request){
        String token = jwtUtil.extractToken(request);
        authService.logout(token);
        return ApiResponse.ok();
    }

    @PostMapping("/find-username")
    @Operation(summary = "아이디 찾기", description = "이메일 포함하여 요청 시 마스킹 처리된 아이디 반환")
    public ApiResponse<Void> findUsername(@RequestBody @Valid EmailRequestDto request){
        authService.findUsername(request);
        return ApiResponse.ok();
    }

}
