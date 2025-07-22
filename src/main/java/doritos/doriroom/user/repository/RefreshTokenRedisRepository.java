package doritos.doriroom.user.repository;

import doritos.doriroom.user.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRedisRepository extends CrudRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);
}
