package com.very.relink.auth.application.result;

import com.very.relink.auth.domain.token.AuthTokens;

public record OAuth2LoginResult(
        Long memberId,
        AuthTokens authTokens
) {
}
