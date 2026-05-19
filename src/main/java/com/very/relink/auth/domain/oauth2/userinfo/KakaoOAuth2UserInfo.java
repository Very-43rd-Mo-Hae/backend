package com.very.relink.auth.domain.oauth2.userinfo;

import com.very.relink.auth.domain.value.OAuth2Provider;
import java.util.Map;

public class KakaoOAuth2UserInfo extends OAuth2UserInfo {

    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public OAuth2Provider getProvider() {
        return OAuth2Provider.KAKAO;
    }

    @Override
    public String getEmail() {
        return kakaoAccountAttribute("email");
    }

    @Override
    public String getName() {
        String nickname = profileAttribute("nickname");
        if (nickname != null) {
            return nickname;
        }

        return kakaoAccountAttribute("name");
    }

    @Override
    public String getImageUrl() {
        String profileImageUrl = profileAttribute("profile_image_url");
        if (profileImageUrl != null) {
            return profileImageUrl;
        }

        return profileAttribute("thumbnail_image_url");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> kakaoAccount() {
        Object kakaoAccount = attributes.get("kakao_account");
        if (kakaoAccount instanceof Map<?, ?>) {
            return (Map<String, Object>) kakaoAccount;
        }

        return Map.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> profile() {
        Object profile = kakaoAccount().get("profile");
        if (profile instanceof Map<?, ?>) {
            return (Map<String, Object>) profile;
        }

        return Map.of();
    }

    private String kakaoAccountAttribute(String key) {
        Object value = kakaoAccount().get(key);
        return value instanceof String ? (String) value : null;
    }

    private String profileAttribute(String key) {
        Object value = profile().get(key);
        return value instanceof String ? (String) value : null;
    }
}
