package doritos.doriroom.atlas.repository;

import doritos.doriroom.atlas.domain.Atlas;
import doritos.doriroom.atlas.domain.UserAtlas;
import doritos.doriroom.user.domain.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAtlasRepository extends JpaRepository<UserAtlas, UUID> {
    Optional<UserAtlas> findByUserAndAtlas(User user, Atlas atlas); // 유저의 특정 지역 도감 조회

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ua FROM UserAtlas ua WHERE ua.user = :user AND ua.atlas = :atlas")
    Optional<UserAtlas> findByUserAndAtlasWithLock(User user, Atlas atlas); // 락으로 동시 발생 제어(경험치 추가 시)

//    List<UserAtlas> findByUser(User user); // 유저의 모든 지역도감 조회 시
    @Query("SELECT ua FROM UserAtlas ua JOIN FETCH ua.atlas WHERE ua.user = :user") // fetch join 하여 UserAtlas와 Atlas를 전부 조회
    List<UserAtlas> findByUserWithAtlas(@Param("user") User user);
}
