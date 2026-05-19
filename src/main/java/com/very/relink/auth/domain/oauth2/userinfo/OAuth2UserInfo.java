package com.very.relink.auth.domain.oauth2.userinfo;

import com.very.relink.auth.domain.value.OAuth2Provider;
import java.util.Map;

public abstract class OAuth2UserInfo {

    protected final Map<String, Object> attributes;

    protected OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getProviderId();

    public abstract OAuth2Provider getProvider();

    public abstract String getEmail();

    public abstract String getName();

    public abstract String getImageUrl();
}
