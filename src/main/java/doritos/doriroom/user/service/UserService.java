package doritos.doriroom.user.service;

import doritos.doriroom.user.exception.DuplicateException;
import doritos.doriroom.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void checkUsernameDuplicate(String username){
        if (userRepository.existsByUsername(username)) {
            throw new DuplicateException("아이디");
        }
    }
    public void checkNicknameDuplicate(String nickname){
        if (userRepository.existsByNickname(nickname)) {
            throw new DuplicateException("닉네임");
        }
    }

}
