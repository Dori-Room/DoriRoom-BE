package doritos.doriroom.atlas.repository;

import doritos.doriroom.atlas.domain.Atlas;
import doritos.doriroom.atlas.domain.UserAtlas;
import doritos.doriroom.user.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAtlasRepository extends JpaRepository<UserAtlas, UUID> {
    Optional<UserAtlas> findByUserAndAtlas(User user, Atlas atlas); // 유저의 특정 지역 도감 조회

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserAtlas> findByUserAndAtlasWithLock(User user, Atlas atlas); // 락으로 동시 발생 제어(경험치 추가 시)

    List<UserAtlas> findByUser(User user); // 유저의 모든 지역도감 조회 시
}
