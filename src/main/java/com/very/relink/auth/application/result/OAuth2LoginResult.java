package com.very.relink.auth.application.result;

import com.very.relink.auth.domain.token.AuthTokens;

public record OAuth2LoginResult(
        Long memberId,
        AuthTokens authTokens,
        boolean requiresAccountRestore
) {
    public static OAuth2LoginResult loggedIn(Long memberId, AuthTokens authTokens) {
        return new OAuth2LoginResult(memberId, authTokens, false);
    }

    public static OAuth2LoginResult requiresAccountRestore(Long memberId) {
        return new OAuth2LoginResult(memberId, null, true);
    }
}
