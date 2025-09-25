package doritos.doriroom.auth.controller;


import doritos.doriroom.auth.dto.request.*;
import doritos.doriroom.auth.dto.response.LoginResponseDto;
import doritos.doriroom.auth.dto.response.UsernameResponseDto;
import doritos.doriroom.auth.dto.response.VerifyCodeResponseDto;
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
    @Operation(summary = "회원가입 시 이메일 인증 번호 발송")
    public ApiResponse<Void> sendVerificationEmail(@RequestBody @Valid EmailRequestDto request){
        authService.sendVerificationEmail(request);
        return ApiResponse.ok();
    }

    @PostMapping("/email/verify")
    @Operation(summary = "회원가입 시 이메일 인증 번호 확인")
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

    @PostMapping("/find-username/email")
    @Operation(summary = "아이디 찾기 이메일 인증 번호 발송", description = "이메일 포함하여 요청 후 인증 코드 포함한 메일 수신")
    public ApiResponse<Void> sendVerificationEmailToFindUsername(@RequestBody @Valid EmailRequestDto request){
        authService.sendVerificationEmailToFindUsername(request);
        return ApiResponse.ok();
    }

    @PostMapping("/find-username/email/verify")
    @Operation(summary = "아이디 찾기 이메일 인증 번호 확인", description = "이메일 인증번호 확인 후 인증 성공 시 아이디 응답")
    public ApiResponse<UsernameResponseDto> verifyEmailAndFindUsername(@RequestBody @Valid EmailVerificationRequestDto request){
        return ApiResponse.ok(authService.verifyEmailAndFindUsername(request));
    }

    @PostMapping("/password/send-code")
    @Operation(summary = "비밀번호 재설정 인증코드 발송", description = "이메일을 입력하여 인증 코드 전송")
    public ApiResponse<Void> sendPasswordResetCode(@RequestBody @Valid EmailRequestDto request) {
        authService.sendPasswordResetCode(request);
        return ApiResponse.ok();
    }

    @PostMapping("/password/verify")
    @Operation(summary = "비밀번호 인증코드 인증", description = "이메일로 받은 인증 코드 일치 여부 확인")
    public ApiResponse<VerifyCodeResponseDto> resetPassword(@RequestBody @Valid EmailVerificationRequestDto request) {
        return ApiResponse.ok(authService.verifyPasswordResetCode(request));
    }

    @PostMapping("/password/reset")
    @Operation(summary = "비밀번호 재설정", description = "비밀번호를 새로 설정하여 반영")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequestDto request) {
        authService.resetPassword(request);
        return ApiResponse.ok();
    }
}
