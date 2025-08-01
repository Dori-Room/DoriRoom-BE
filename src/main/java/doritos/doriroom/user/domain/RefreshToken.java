package doritos.doriroom.user.domain;

import org.springframework.data.annotation.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import java.util.UUID;

@Getter
@Builder
@RedisHash(value = "refresh_token")
public class RefreshToken {
    @Id
    private UUID userId;

    @Indexed
    private String refreshToken;
    @TimeToLive
    private Long ttl;
}
