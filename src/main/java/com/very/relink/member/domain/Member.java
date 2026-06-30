package com.very.relink.member.domain;

import com.very.relink.auth.domain.value.OAuth2Provider;
import java.time.LocalDateTime;
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
    private String bio;
    private String imageUrl;
    private OAuth2Provider provider;
    private String providerId;
    @Builder.Default
    private MemberStatus status = MemberStatus.ACTIVE;
    private LocalDateTime withdrawnAt;

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
                .status(MemberStatus.ACTIVE)
                .build();
    }

    public void updateProfile(String name, String bio, String imageUrl) {
        this.name = name;
        this.bio = bio;
        this.imageUrl = imageUrl;
    }

    public boolean isWithdrawn() {
        return status == MemberStatus.WITHDRAWN;
    }

    public void withdraw(LocalDateTime withdrawnAt) {
        this.status = MemberStatus.WITHDRAWN;
        this.withdrawnAt = withdrawnAt;
    }
}
