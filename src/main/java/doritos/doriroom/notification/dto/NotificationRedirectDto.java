package doritos.doriroom.notification.dto;

public record NotificationRedirectDto(
        String redirectUrl
) {
    public static NotificationRedirectDto of(String redirectUrl) {
        return new NotificationRedirectDto(redirectUrl);
    }
}
