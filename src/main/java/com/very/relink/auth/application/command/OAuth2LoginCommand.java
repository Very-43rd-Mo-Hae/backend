package com.very.relink.auth.application.command;

import com.very.relink.auth.domain.value.OAuth2Provider;

public record OAuth2LoginCommand(
        OAuth2Provider provider,
        String email,
        String name,
        String imageUrl
) {
}
