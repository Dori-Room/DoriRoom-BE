package doritos.doriroom.user.service;

import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.dto.SignupRequestDto;
import doritos.doriroom.user.exception.DuplicateException;
import doritos.doriroom.user.repository.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Builder
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public void signup(SignupRequestDto request) {
        // 중복 아이디, 닉네임 예외 처리
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateException("아이디");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateException("닉네임");
        }

        User user = User.builder()
                .userId(UUID.randomUUID())
                .username(request.getUsername())
                .password(encoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();

        userRepository.save(user);
    }

}
