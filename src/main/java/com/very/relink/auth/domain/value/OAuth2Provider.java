package com.very.relink.auth.domain.value;

import java.util.Arrays;
import lombok.Getter;

@Getter
public enum OAuth2Provider {

    GOOGLE("google"),
    KAKAO("kakao"),
    APPLE("apple");

    private final String registrationId;

    OAuth2Provider(String registrationId) {
        this.registrationId = registrationId;
    }

    public static OAuth2Provider fromRegistrationId(String registrationId) {
        return Arrays.stream(values())
                .filter(provider -> provider.registrationId.equals(registrationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported OAuth2 provider: " + registrationId));
    }
}
