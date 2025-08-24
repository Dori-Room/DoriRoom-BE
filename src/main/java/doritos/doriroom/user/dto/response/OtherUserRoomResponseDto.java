package doritos.doriroom.user.dto.response;

import doritos.doriroom.item.dto.response.EquippedItemResponse;
import doritos.doriroom.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record OtherUserRoomResponseDto(
    @Schema(description = "사용자 ID", example = "43d1a5a2-58dc-4786-8dba-27c62cae1943")
    UUID userId,

    @Schema(description = "사용자 닉네임", example = "도리토스")
    String nickname,

    @Schema(description = "방 주인 착용 아이템 목록")
    List<EquippedItemResponse> equippedItems,

    @Schema(description = "투데이 수", example = "128")
    int viewCount,

    @Schema(description = "좋아요 수", example = "500")
    int likeCount,

    @Schema(description = "팔로우 여부", example = "true")
    boolean following
){
    public static OtherUserRoomResponseDto from(User user, List<EquippedItemResponse> equippedItems) {
        if (user == null) {
            return null;
        }

        return OtherUserRoomResponseDto.builder()
            .userId(user.getUserId())
            .nickname(user.getNickname())
            .equippedItems(equippedItems)
            .viewCount(user.getViewCount())
            .likeCount(user.getLikeCount())
            .build();
    }
}
