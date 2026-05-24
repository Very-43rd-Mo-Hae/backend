package com.very.relink.auth.adapter.out.social.kakao;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "social.kakao")
public record KakaoSocialProperties(
        String userInfoUri
) {

    public KakaoSocialProperties {
        if (userInfoUri == null || userInfoUri.isBlank()) {
            userInfoUri = "https://kapi.kakao.com/v2/user/me";
        }
    }
}
