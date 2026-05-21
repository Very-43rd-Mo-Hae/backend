package com.very.relink.member.domain;

import com.very.relink.auth.domain.value.OAuth2Provider;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member {

    private final Long id;
    private String email;
    private String name;
    private String imageUrl;
    private OAuth2Provider provider;
    private String providerId;

    public static Member create(String email, String name, String imageUrl) {
        return create(email, name, imageUrl, null, null);
    }

    public static Member create(
            String email,
            String name,
            String imageUrl,
            OAuth2Provider provider,
            String providerId
    ) {
        return Member.builder()
                .email(email)
                .name(name)
                .imageUrl(imageUrl)
                .provider(provider)
                .providerId(providerId)
                .build();
    }

    public void updateProfile(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }
}
