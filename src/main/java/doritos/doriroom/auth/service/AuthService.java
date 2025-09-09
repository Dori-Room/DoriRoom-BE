package doritos.doriroom.auth.service;

import doritos.doriroom.auth.dto.request.*;
import doritos.doriroom.auth.dto.response.LoginResponseDto;
import doritos.doriroom.auth.dto.response.UsernameResponseDto;
import doritos.doriroom.auth.exception.*;
import doritos.doriroom.auth.template.EmailTemplate;
import doritos.doriroom.global.jwt.JwtUtil;
import doritos.doriroom.auth.domain.RefreshToken;
import doritos.doriroom.item.domain.Item;
import doritos.doriroom.item.domain.UserItem;
import doritos.doriroom.item.repository.ItemRepository;
import doritos.doriroom.item.repository.UserItemRepository;
import doritos.doriroom.item.service.ItemService;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final UserItemRepository userItemRepository;

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

        // 이메일 본문 구성 후 발송
        String content = emailTemplate.createVerificationEmailContent(verificationCode);
        sendEmail(email, EmailTemplate.Subject.VERIFICATION, content);
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

        List<Item> defaultItems = itemRepository.findByDefaultItemTrue(); // 기본 아이템 전부 조회

        // 신규 유저 기본 아이템 지급 및 착용
        List<UserItem> newUserItems = defaultItems.stream()
                .map(item -> UserItem.builder()
                        .user(user)
                        .item(item)
                        .equipped(true) // 착용 상태로 지정
                        .build())
                .collect(Collectors.toList());
        userItemRepository.saveAll(newUserItems);

        redisTemplate.delete(verifiedKey); // 유저 등록 후 인증 상태 삭제
    }

    public LoginResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(UserNotFoundException::new);

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

        if(username == null)
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

    /* 아이디 찾기 / 비밀번호 재설정 */

    // 아이디 찾기
    public void sendVerificationEmailToFindUsername(EmailRequestDto request){
        Optional<User> user = userRepository.findByEmail(request.email());
        if (user.isEmpty()) { // 유저가 존재하지 않는 경우에 스킵함
            return;
        }
        String verificationCode = String.format("%06d", new SecureRandom().nextInt(1000000)); // 6자리 인증 코드 생성

        // redis에 인증 코드 저장
        String verificationKey = FIND_USERNAME_KEY_PREFIX.getValue() + request.email();
        redisTemplate.opsForValue().set(verificationKey, verificationCode, Duration.ofSeconds(VERIFICATION_EXPIRE_SECONDS));

        String content = emailTemplate.createFindUsernameEmailContent(verificationCode);
        sendEmail(user.get().getEmail(), EmailTemplate.Subject.FIND_USERNAME, content);
    }

    public UsernameResponseDto verifyEmailAndFindUsername(EmailVerificationRequestDto request){
        // 인증 코드 조회
        String verificationKey = FIND_USERNAME_KEY_PREFIX.getValue() + request.email();
        String storedCode = (String) redisTemplate.opsForValue().get(verificationKey);

        if (storedCode == null || !storedCode.equals(request.verificationCode())) {
            throw new InvalidOrExpiredVerificationCodeException();
        }

        User user = userRepository.findByEmail(request.email()).orElseThrow(UserNotFoundException::new);

        redisTemplate.delete(verificationKey); // 인증 성공 후 삭제

        return UsernameResponseDto.of(user.getUsername());
    }

    // 비밀번호 재설정 1. 이메일 인증 코드 전송
    public void sendPasswordResetCode(EmailRequestDto request){
        var user = userRepository.findByEmail(request.email());
        if (user.isEmpty()) { // 유저가 존재하지 않는 경우에 스킵함
            return;
        }

        String verificationCode = String.format("%06d", new SecureRandom().nextInt(1000000)); // 6자리 인증 코드

        // redis에 인증 코드 저장
        String verificationKey = RESET_CODE_PREFIX.getValue() + request.email();
        redisTemplate.opsForValue().set(verificationKey, verificationCode, Duration.ofSeconds(VERIFICATION_EXPIRE_SECONDS));

        // 이메일 본문 구성 후 발송
        String content = emailTemplate.createPasswordResetEmailContent(verificationCode);
        sendEmail(request.email(), EmailTemplate.Subject.PASSWORD_RESET, content);

    }

    // 비밀전호 재설정 2. 비밀번호 변경
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request){
        // 인증 코드 조회
        String verificationKey = RESET_CODE_PREFIX.getValue() + request.email();
        String storedCode = (String) redisTemplate.opsForValue().get(verificationKey);

        if (storedCode == null || !storedCode.equals(request.code())) {
            throw new InvalidOrExpiredVerificationCodeException();
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(UserNotFoundException::new);

        user.setPassword(encoder.encode(request.newPassword())); // 새로운 비밀번호로 설정
        redisTemplate.delete(verificationKey); // 사용된 인증코드 삭제
    }

    /* 내부 메서드 */

    // 이메일 발송 메서드
    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); // true: HTML 형식으로 발송

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new EmailSendFailedException();
        }
    }

    // 아이디 마스킹 처리 메서드
    // 아이디 찾기 시 username 길이가 3 이하인 경우 인덱스 에러 방지
    private String maskUsername(String username) {
        if (username == null || username.isEmpty()) return "***";
        int len = username.length();
        if (len == 1) return "*";
        if (len == 2) return username.charAt(0) + "*";
        if (len == 3) return username.charAt(0) + "*" + username.charAt(2);
        return username.substring(0, 2) + "*".repeat(len - 4) + username.substring(len - 2);
    }
}