package com.very.relink.auth.application.port.out;

import com.very.relink.auth.domain.token.RefreshTokenClaims;

public interface RefreshTokenIssuePort {

    String issueRefreshToken(Long memberId, String sessionId, String refreshTokenJti);

    RefreshTokenClaims authenticateRefreshToken(String refreshToken);
}
