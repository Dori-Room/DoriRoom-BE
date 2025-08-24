package doritos.doriroom.user.dto.response;

import doritos.doriroom.item.dto.response.EquippedItemResponse;
import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record MyRoomResponseDto(
    @Schema(description = "내 유저 ID", example = "43d1a5a2-58dc-4786-8dba-27c62cae1943")
    UUID userId,

    @Schema(description = "내 닉네임", example = "도리토스")
    String nickname,

    @Schema(description = "내가 착용 중인 아이템 목록")
    List<EquippedItemResponse> equippedItems,

    @Schema(description = "내 방 조회수", example = "42")
    int viewCount,

    @Schema(description = "내 방 좋아요 수", example = "15")
    int likeCount,

    @Schema(description = "내 크레딧", example = "10000")
    Long credit
) {
    public static MyRoomResponseDto from(User user, List<EquippedItemResponse> equippedItems) {
        if (user == null) {
            return null;
        }

        return MyRoomResponseDto.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .equippedItems(equippedItems)
            .viewCount(user.getViewCount())
            .likeCount(user.getLikeCount())
            .credit(user.getCredit())
            .build();
    }
}
