package doritos.doriroom.auth.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthConstants {

    BLACKLIST_KEY_PREFIX("blacklist_token:"),
    VERIFICATION_KEY_PREFIX("email_verification:"),
    VERIFIED_KEY_PREFIX("email_verified:");

    private final String value;
}