package com.very.relink.auth.domain.oauth2.userinfo;

import com.very.relink.auth.domain.value.OAuth2Provider;
import java.util.Map;

public class AppleOAuth2UserInfo extends OAuth2UserInfo {

    public AppleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProviderId() {
        return String.valueOf(attributes.get("sub"));
    }

    @Override
    public OAuth2Provider getProvider() {
        return OAuth2Provider.APPLE;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        Object name = attributes.get("name");
        if (name instanceof String) {
            return (String) name;
        }

        return null;
    }

    @Override
    public String getImageUrl() {
        return null;
    }
}
