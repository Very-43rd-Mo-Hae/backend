package com.very.relink.auth.adapter.in.security;

import com.very.relink.auth.domain.token.AuthenticatedMember;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetail implements UserDetails {

    private static final List<GrantedAuthority> DEFAULT_AUTHORITIES = List.of(
            new SimpleGrantedAuthority("ROLE_USER")
    );

    private final AuthenticatedMember authenticatedMember;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetail(AuthenticatedMember authenticatedMember) {
        this(authenticatedMember, DEFAULT_AUTHORITIES);
    }

    public CustomUserDetail(
            AuthenticatedMember authenticatedMember,
            Collection<? extends GrantedAuthority> authorities
    ) {
        this.authenticatedMember = authenticatedMember;
        this.authorities = List.copyOf(authorities);
    }

    public Long getMemberId() {
        return authenticatedMember.memberId();
    }

    public String getEmail() {
        return authenticatedMember.email();
    }

    public String getName() {
        return authenticatedMember.name();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return authenticatedMember.email();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
