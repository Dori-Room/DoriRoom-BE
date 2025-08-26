package doritos.doriroom.follow.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record FollowUserInfoDto (
        UUID userId,
        String nickname,
        String profileImageUrl,
        boolean isFollowing,
        boolean isFollowedBy,
        boolean isBestFriend,
        LocalDateTime followedAt
){}