package doritos.doriroom.guestbook.dto.response;

import doritos.doriroom.guestbook.domain.Guestbook;
import doritos.doriroom.item.dto.response.EquippedItemResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Schema(description = "방명록 응답")
public record GuestbookResponseDto(
    @Schema(description = "방명록 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID guestbookId,

    @Schema(description = "방명록 내용", example = "안녕하세요! 방문했습니다.")
    String content,

    @Schema(description = "작성자 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID writerId,

    @Schema(description = "작성자 닉네임", example = "도리도리")
    String writerNickname,

    @Schema(description = "작성자 캐릭터")
    List<EquippedItemResponse> writerEquippedItems,

    @Schema(description = "방 주인 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID roomOwnerId,

    @Schema(description = "생성 날짜", example = "2024.01.15 14:30:25")
    String createdAt
) {
    public static GuestbookResponseDto from(Guestbook guestbook, String writerNickname, List<EquippedItemResponse> equippedItemResponses) {
        return new GuestbookResponseDto(
            guestbook.getGuestbookId(),
            guestbook.getContent(),
            guestbook.getWriterId(),
            writerNickname,
            equippedItemResponses,
            guestbook.getRoomOwnerId(),
            guestbook.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
}
