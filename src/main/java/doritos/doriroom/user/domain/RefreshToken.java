package doritos.doriroom.user.domain;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.util.UUID;

@Getter
@Builder
@RedisHash(value = "refresh_token")
public class RefreshToken {
    @Id
    private UUID userId;
    private String refreshToken;
    @TimeToLive
    private Long ttl; // ms
}
