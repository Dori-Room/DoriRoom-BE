package doritos.doriroom.notification.domain;

import lombok.*;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    FOLLOWER("%s님이 회원님을 팔로우하기 시작했습니다."),
    GUESTBOOK_ENTRY("%s님이 방명록을 남겼습니다."),
    CHALLENGE_REWARD("도전과제 '%s'를 완료했습니다! 보상을 받아주세요."),
    DIARY_LIKE("%s님이 회원님의 일기를 좋아합니다."),

    TEST_MESSAGE("테스트 메세지 발송!");

    private final String message;

    public String createContent(String placeholder) {
        return String.format(this.message, placeholder);
    }
}
