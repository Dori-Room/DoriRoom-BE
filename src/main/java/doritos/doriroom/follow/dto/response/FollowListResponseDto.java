package doritos.doriroom.follow.dto.response;

import java.util.List;

public record FollowListResponseDto( // 유저 목록 및 총합
        List<FollowUserInfoDto> users,
        int totalCount
){}