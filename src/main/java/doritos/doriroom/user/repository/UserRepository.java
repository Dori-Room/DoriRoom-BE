package doritos.doriroom.user.repository;

import doritos.doriroom.user.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // username, nickname, email 중복 검사
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);

    // 로그인 시 유저 조회
    Optional<User> findByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.userId IN :userIds")
    List<User> findByUserIdIn(@Param("userIds") List<UUID> userIds);
}
