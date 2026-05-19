package com.very.relink.auth.application.service;

import com.very.relink.auth.domain.token.AuthTokens;
import java.util.Collection;
import java.util.Map;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

public class OAuth2AuthenticatedUser implements OAuth2User {

    private final OAuth2User delegate;
    @Getter
    private final Long memberId;
    @Getter
    private final AuthTokens authTokens;

    public OAuth2AuthenticatedUser(OAuth2User delegate, Long memberId, AuthTokens authTokens) {
        this.delegate = delegate;
        this.memberId = memberId;
        this.authTokens = authTokens;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return delegate.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return delegate.getAuthorities();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }
}
