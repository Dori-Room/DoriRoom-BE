package doritos.doriroom.atlas.repository;

import doritos.doriroom.atlas.domain.Atlas;
import doritos.doriroom.atlas.domain.UserAtlas;
import doritos.doriroom.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserAtlasRepository extends JpaRepository<UserAtlas, UUID> {
    Optional<UserAtlas> findByUserAndAtlas(User user, Atlas atlas); // 유저의 특정 지역 도감 조회

    List<UserAtlas> findByUser(User user); // 유저의 모든 지역도감 조회 시
}
