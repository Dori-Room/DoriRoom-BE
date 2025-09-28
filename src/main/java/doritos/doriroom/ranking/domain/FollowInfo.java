package doritos.doriroom.ranking.domain;

import doritos.doriroom.follow.domain.Follow;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record FollowInfo(
    Map<UUID, Follow> followingMap,
    Set<UUID> followedByUserIds
) {
}
