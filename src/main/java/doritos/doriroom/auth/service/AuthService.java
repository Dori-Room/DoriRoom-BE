package doritos.doriroom.auth.service;

import doritos.doriroom.auth.dto.request.*;
import doritos.doriroom.auth.dto.response.LoginResponseDto;
import doritos.doriroom.auth.exception.InvalidPasswordException;
import doritos.doriroom.global.exception.ApiException;
import doritos.doriroom.global.jwt.JwtUtil;
import doritos.doriroom.auth.domain.RefreshToken;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.exception.DuplicateException;
import doritos.doriroom.auth.repository.RefreshTokenRedisRepository;
import doritos.doriroom.user.exception.UsernameNotFoundException;
import doritos.doriroom.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public void sendVerificationEmail(EmailRequest request){

    }

    public void verifyEmail(EmailVerificationRequest request){

    }

    public void signup(SignupRequestDto request) {
        // 중복 아이디, 닉네임 예외 처리
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateException("아이디");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateException("닉네임");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateException("이메일");
        }

        User user = User.builder()
                .userId(UUID.randomUUID())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();

        userRepository.save(user);
    }

    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException());

        if (!encoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefresh(user);

        return new LoginResponseDto(accessToken, refreshToken);
    }

    public TokenResponseDto reissue(RefreshTokenRequestDto request) {
        jwtUtil.validateToken(request.getRefreshToken());

        RefreshToken storedToken = refreshTokenRedisRepository.findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "존재하지 않는 리프레시 토큰입니다."));

        UUID userId = storedToken.getUserId();
        String username = jwtUtil.getUsernameFromToken(storedToken.getRefreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."));

        refreshTokenRedisRepository.deleteById(userId);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefresh(user);

        return new TokenResponseDto(accessToken, refreshToken);
    }
}
