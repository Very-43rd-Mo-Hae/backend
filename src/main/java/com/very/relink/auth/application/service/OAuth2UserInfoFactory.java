package com.very.relink.auth.application.service;

import com.very.relink.auth.domain.oauth2.userinfo.AppleOAuth2UserInfo;
import com.very.relink.auth.domain.oauth2.userinfo.GoogleOAuth2UserInfo;
import com.very.relink.auth.domain.oauth2.userinfo.KakaoOAuth2UserInfo;
import com.very.relink.auth.domain.oauth2.userinfo.OAuth2UserInfo;
import com.very.relink.auth.domain.value.OAuth2Provider;
import com.very.relink.auth.exception.AuthErrorCode;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OAuth2UserInfoFactory {

    public OAuth2UserInfo create(
            OAuth2Provider oAuth2Provider,
            Map<String, Object> attributes
    ) {
        switch (oAuth2Provider) {
            case GOOGLE -> {
                return new GoogleOAuth2UserInfo(attributes);
            }
            case KAKAO -> {
                return new KakaoOAuth2UserInfo(attributes);
            }
            case APPLE -> {
                log.warn("Apple login -> 추후 구현 예정");
                throw AuthErrorCode.UNSUPPORTED_OAUTH2_PROVIDER.toException();
            }
        }

        throw AuthErrorCode.UNSUPPORTED_OAUTH2_PROVIDER.toException();
    }
}
