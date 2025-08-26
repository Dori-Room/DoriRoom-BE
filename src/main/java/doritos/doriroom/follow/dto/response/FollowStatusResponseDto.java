package doritos.doriroom.follow.dto.response;

import lombok.Builder;

@Builder
public record FollowStatusResponseDto (
        boolean isFollowing,
        boolean isFollowedBy,     // 상대가 나를 팔로우하는지
        boolean isBestFriend,
        boolean isMutualFollow    // 서로 팔로우인지
){}