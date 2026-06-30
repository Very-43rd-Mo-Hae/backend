package com.very.relink.member.application.response;

import com.very.relink.auth.domain.value.OAuth2Provider;
import com.very.relink.member.domain.Member;

public record MyPageResponse(
        Long memberId,
        String name,
        String bio,
        Long friendCount,
        OAuth2Provider signupProvider,
        String accountId,
        String email,
        String imageUrl,
        boolean active
) {

    public static MyPageResponse of(Member member, long friendCount) {
        return new MyPageResponse(
                member.getId(),
                member.getName(),
                member.getBio(),
                friendCount,
                member.getProvider(),
                String.valueOf(member.getId()),
                member.getEmail(),
                member.getImageUrl(),
                !member.isWithdrawn()
        );
    }
}
