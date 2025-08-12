package doritos.doriroom.auth.service;

import doritos.doriroom.auth.dto.request.*;
import doritos.doriroom.auth.dto.response.LoginResponseDto;
import doritos.doriroom.auth.exception.*;
import doritos.doriroom.global.jwt.JwtUtil;
import doritos.doriroom.auth.domain.RefreshToken;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.exception.DuplicateException;
import doritos.doriroom.auth.repository.RefreshTokenRedisRepository;
import doritos.doriroom.user.exception.UsernameNotFoundException;
import doritos.doriroom.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender mailSender;

    // redis 키
    private static final String VERIFICATION_KEY_PREFIX = "email_verification:";
    private static final String VERIFIED_KEY_PREFIX = "email_verified:";

    // ttl
    private static final long VERIFICATION_EXPIRE_SECONDS = 300; // 5분 (인증 번호 확인 시간)
    private static final long VERIFIED_EXPIRE_SECONDS = 3600;    // 1시간(인증 성공 유효 시간)_

    public void sendVerificationEmail(EmailRequest request){
        String email = request.getEmail();

        if (userRepository.existsByEmail(email)) { // 중복 이메일 확인
            throw new DuplicateException("이메일");
        }

        String verificationCode = String.format("%06d", new Random().nextInt(1000000)); // 6자리 인증 코드

        // redis에 인증 코드 저장
        String verificationKey = VERIFICATION_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(verificationKey, verificationCode, Duration.ofSeconds(VERIFICATION_EXPIRE_SECONDS));

        sendEmail(email, verificationCode);
    }

    public void verifyEmail(EmailVerificationRequest request){
        String email = request.getEmail();
        String input = request.getVerificationCode();

        // 인증 코드 조회
        String verificationKey = VERIFICATION_KEY_PREFIX + email;
        String storedCode = (String) redisTemplate.opsForValue().get(verificationKey);

        if (storedCode == null || !storedCode.equals(input)) {
            throw new InvalidOrExpiredVerificationCodeException();
        }

        redisTemplate.delete(verificationKey); // 인증 성공 후 삭제

        String verifiedKey = VERIFIED_KEY_PREFIX + email; // 인증됨 상태 저장
        redisTemplate.opsForValue().set(verifiedKey, "verified", Duration.ofSeconds(VERIFIED_EXPIRE_SECONDS));
    }

    public void signup(SignupRequestDto request) {
        // 이메일 인증 완료 여부 확인
        String verifiedKey = VERIFIED_KEY_PREFIX + request.getEmail();
        String verified = (String) redisTemplate.opsForValue().get(verifiedKey);

        if (verified == null) {
            throw new EmailNotVerifiedException();
        }

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

        redisTemplate.delete(verifiedKey); // 유저 등록 후 인증 상태 삭제
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
                .orElseThrow(RefreshTokenNotFoundException::new);

        UUID userId = storedToken.getUserId();
        String username = jwtUtil.getUsernameFromToken(storedToken.getRefreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(UsernameNotFoundException::new);

        refreshTokenRedisRepository.deleteById(userId);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefresh(user);

        return new TokenResponseDto(accessToken, refreshToken);
    }

    private void sendEmail(String email, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("[DoriRoom] 이메일 인증번호");
            helper.setText(createEmailContent(verificationCode), true); // 이메일 contect 구성

            mailSender.send(message); // 이메일 전송
        } catch (MessagingException e) {
            throw new EmailSendFailedException();
        }
    }

    private String createEmailContent(String verificationCode) { // 이메일 content 예시
        return """
            <div style="max-width: 600px; margin: 0 auto; padding: 20px; font-family: Arial, sans-serif;">
                <h2 style="color: #333;">이메일 인증</h2>
                <p>안녕하세요! DoriRoom입니다.</p>
                <p>아래 인증번호를 입력하여 이메일 인증을 완료해주세요.</p>
                <div style="background-color: #f5f5f5; padding: 20px; text-align: center; margin: 20px 0;">
                    <h1 style="color: #007bff; margin: 0; letter-spacing: 5px;">%s</h1>
                </div>
                <p><strong>인증번호는 5분간 유효합니다.</strong></p>
                <p>감사합니다.</p>
            </div>
            """.formatted(verificationCode);
    }
}
