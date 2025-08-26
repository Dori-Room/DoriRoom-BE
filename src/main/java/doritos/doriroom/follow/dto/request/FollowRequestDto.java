package doritos.doriroom.follow.dto.request;

import java.util.UUID;

public record FollowRequestDto( //팔로우 요청 시에만 사용
        UUID targetUserId
){}