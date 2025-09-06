package doritos.doriroom.auth.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthConstants {

    BLACKLIST_KEY_PREFIX("blacklist_token:"),
    BLACKLIST_VALUE("blacklisted"),
    VERIFICATION_KEY_PREFIX("email_verification:"),
    VERIFIED_KEY_PREFIX("email_verified:"),
    RESET_CODE_PREFIX("reset_code:");

    private final String value;
}