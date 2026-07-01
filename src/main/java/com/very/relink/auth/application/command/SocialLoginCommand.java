package com.very.relink.auth.application.command;

import com.very.relink.auth.domain.value.OAuth2Provider;

public record SocialLoginCommand(
        OAuth2Provider provider,
        String idToken,
        String accessToken,
        String name,
        String deviceId,
        String deviceName,
        String userAgent,
        boolean restoreAccount
) {
}
