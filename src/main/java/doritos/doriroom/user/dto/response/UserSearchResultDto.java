package doritos.doriroom.user.dto.response;

import java.util.UUID;

public record UserSearchResultDto(
        UUID userId,
        String nickname,
        String profileImageUrl,
        boolean isFollowing,
        boolean isFollowedBy,
        boolean isBestFriend
) {}