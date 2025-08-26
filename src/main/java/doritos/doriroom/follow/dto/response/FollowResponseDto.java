package doritos.doriroom.follow.dto.response;

import doritos.doriroom.follow.domain.Follow;
import java.time.LocalDateTime;
import java.util.UUID;


public record FollowResponseDto (
        UUID followId,

        UUID followerId,
        UUID followedId,
        String followerNickname,
        String followedNickname,

        boolean isBestFriend,
//        boolean isMutual,
        LocalDateTime createdAt
) {
    public static FollowResponseDto from(Follow follow) {
        return new FollowResponseDto(
                follow.getId(),
                follow.getFollower().getUserId(),
                follow.getFollowed().getUserId(),
                follow.getFollower().getNickname(),
                follow.getFollowed().getNickname(),
                follow.isBestFriend(),
//                isMutual,
                follow.getCreatedAt()
        );
    }
}