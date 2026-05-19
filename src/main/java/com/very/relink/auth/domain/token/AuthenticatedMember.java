package com.very.relink.auth.domain.token;

public record AuthenticatedMember(
        Long memberId,
        String email,
        String name
) {
}
