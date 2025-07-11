package doritos.doriroom.user.repository;

import doritos.doriroom.user.domain.User;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // username, nickname 중복 검사
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);

    // 로그인 시 유저 조회
    User findByUsername(String username);
}
