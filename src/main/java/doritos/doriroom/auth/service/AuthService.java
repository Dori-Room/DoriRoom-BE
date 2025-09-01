package doritos.doriroom.auth.service;

import doritos.doriroom.auth.dto.request.*;
import doritos.doriroom.auth.dto.response.LoginResponseDto;
import doritos.doriroom.auth.exception.*;
import doritos.doriroom.auth.template.EmailTemplate;
import doritos.doriroom.global.jwt.JwtUtil;
import doritos.doriroom.auth.domain.RefreshToken;
import doritos.doriroom.s3.S3Uploader;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.exception.DuplicateException;
import doritos.doriroom.auth.repository.RefreshTokenRedisRepository;
import doritos.doriroom.user.exception.UserNotFoundException;
import doritos.doriroom.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Random;
import java.util.UUID;

import static doritos.doriroom.auth.common.AuthConstants.*;

@Slf4j
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
    private final EmailTemplate emailTemplate;
    private final S3Uploader s3Uploader;

    // ttl
    private static final long VERIFICATION_EXPIRE_SECONDS = 300; // 5분 (인증 번호 확인 시간)
    private static final long VERIFIED_EXPIRE_SECONDS = 3600;    // 1시간(인증 성공 유효 시간)


    public void sendVerificationEmail(EmailRequestDto request){
        String email = request.email();

        if (userRepository.existsByEmail(email)) { // 중복 이메일 확인
            throw new DuplicateException("이메일");
        }

        String verificationCode = String.format("%06d", new Random().nextInt(1000000)); // 6자리 인증 코드

        // redis에 인증 코드 저장
        String verificationKey = VERIFICATION_KEY_PREFIX.getValue() + email;
        redisTemplate.opsForValue().set(verificationKey, verificationCode, Duration.ofSeconds(VERIFICATION_EXPIRE_SECONDS));

        sendEmail(email, verificationCode);
    }

    public void verifyEmail(EmailVerificationRequestDto request){
        String email = request.email();
        String input = request.verificationCode();

        // 인증 코드 조회
        String verificationKey = VERIFICATION_KEY_PREFIX.getValue() + email;
        String storedCode = (String) redisTemplate.opsForValue().get(verificationKey);

        if (storedCode == null || !storedCode.equals(input)) {
            throw new InvalidOrExpiredVerificationCodeException();
        }

        redisTemplate.delete(verificationKey); // 인증 성공 후 삭제

        String verifiedKey = VERIFIED_KEY_PREFIX.getValue() + email; // 인증됨 상태 저장
        redisTemplate.opsForValue().set(verifiedKey, "verified", Duration.ofSeconds(VERIFIED_EXPIRE_SECONDS));
    }

    public void signup(SignupRequestDto request, MultipartFile image) {
        // 이메일 인증 완료 여부 확인
        String verifiedKey = VERIFIED_KEY_PREFIX.getValue() + request.email();
        String verified = (String) redisTemplate.opsForValue().get(verifiedKey);

        if (verified == null) {
            throw new EmailNotVerifiedException();
        }

        // 중복 아이디, 닉네임 예외 처리
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateException("아이디");
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new DuplicateException("닉네임");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateException("이메일");
        }

        // 프로필 이미지 등록
        String profileImageUrl = null;
        if (image != null && !image.isEmpty()){
            profileImageUrl = s3Uploader.uploadFile(image, "profile_image"); // s3에 업로드 후 url 반환
        }

        User user = User.builder()
                .userId(UUID.randomUUID())
                .username(request.username())
                .email(request.email())
                .password(encoder.encode(request.password()))
                .nickname(request.nickname())
                .profileImageUrl(profileImageUrl)
                .build();

        userRepository.save(user);
        redisTemplate.delete(verifiedKey); // 유저 등록 후 인증 상태 삭제
    }

    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UserNotFoundException());

        if (!encoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefresh(user);

        return new LoginResponseDto(accessToken, refreshToken);
    }

    public TokenResponseDto reissue(RefreshTokenRequestDto request) {
        jwtUtil.validateToken(request.refreshToken());

        RefreshToken storedToken = refreshTokenRedisRepository.findByRefreshToken(request.refreshToken())
                .orElseThrow(RefreshTokenNotFoundException::new);

        UUID userId = storedToken.getUserId();
        String username = jwtUtil.getUsernameFromToken(storedToken.getRefreshToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);

        refreshTokenRedisRepository.deleteById(userId);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefresh(user);

        return new TokenResponseDto(accessToken, refreshToken);
    }

    public void logout(String accessToken) {
        Claims claims = jwtUtil.parseClaims(accessToken);
        String username = claims.getSubject();

        if(username == null | username.isBlank())
            throw new InvalidTokenException("토큰에서 username을 추출할 수 없습니다.");

        User user = userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);

        refreshTokenRedisRepository.deleteById(user.getUserId()); // 추출한 User의 리프레시 토큰 삭제

        // 토큰의 남은 유효시간 동안 redis에 저장하여 블랙리스트 처리
        long remainingTimeMillis = claims.getExpiration().getTime() - System.currentTimeMillis();
        if (remainingTimeMillis > 0) {
            String tokenKey = BLACKLIST_KEY_PREFIX.getValue() + accessToken;
            redisTemplate.opsForValue().set(tokenKey, BLACKLIST_VALUE, Duration.ofMillis(remainingTimeMillis));
        }
    }

    private void sendEmail(String email, String verificationCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject(EmailTemplate.Subject.VERIFICATION);
            helper.setText(emailTemplate.createVerificationEmailContent(verificationCode), true); // 이메일 contect 구성

            mailSender.send(message); // 이메일 전송
        } catch (MessagingException e) {
            throw new EmailSendFailedException();
        }
    }
}
