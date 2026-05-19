package com.very.relink.auth.domain.value;

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
}
