package doritos.doriroom.diary.dto.response;

import doritos.doriroom.diary.domain.Diary;
import doritos.doriroom.event.domain.Event;
import doritos.doriroom.event.dto.response.EventInfoResponseDto;
import doritos.doriroom.user.domain.RoomVisibility;
import doritos.doriroom.user.domain.User;
import doritos.doriroom.user.dto.response.UserInfoResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Schema(description = "일기 응답")
public record DiaryResponseDto(
    @Schema(description = "일기 ID", example = "3071dc72-0b37-45b4-95ca-465615636868")
    UUID diaryId,
    
    @Schema(description = "일기 내용", example = "오늘 축제에 다녀왔습니다. 정말 즐거웠어요!")
    String content,
    
    @Schema(description = "이미지 URL 목록", example = "['https://example.com/image1.jpg', 'https://example.com/image2.jpg']")
    List<String> imageUrls,
    
    @Schema(description = "방 공개 설정", example = "PUBLIC")
    RoomVisibility visibility,
    
    @Schema(description = "좋아요 수", example = "5")
    int likes,

    @Schema(description = "지급된 크레딧", example = "3")
    int credit,

    @Schema(description = "방문 날짜", example = "2025-08-10")
    String visitedAt,
    
    @Schema(description = "생성 일시", example = "2025-08-11T14:30:00")
    String createdAt,

    @Schema(description = "작성자 정보")
    UserInfoResponseDto userInfo,

    @Schema(description = "축제 정보")
    EventInfoResponseDto eventInfo
) {
    public static DiaryResponseDto from(Diary diary, int totalCredit, User user, Event event) {
        return new DiaryResponseDto(
            diary.getDiaryId(),
            diary.getContent(),
            diary.getImageUrls(),
            diary.getDiaryVisibility(),
            diary.getLikes(),
            totalCredit,
            diary.getVisitedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            diary.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            UserInfoResponseDto.from(user),
            EventInfoResponseDto.from(event)
        );
    }
}
