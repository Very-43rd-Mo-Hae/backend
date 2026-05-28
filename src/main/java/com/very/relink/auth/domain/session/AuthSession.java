package com.very.relink.auth.domain.session;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthSession {

    private final Long id;
    private final String sessionId;
    private final Long memberId;
    private final String deviceId;
    private final String deviceName;
    private final String userAgent;
    private String refreshTokenJti;
    private String refreshTokenHash;
    private AuthSessionStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;
    private final LocalDateTime expiresAt;
    private LocalDateTime loggedOutAt;

    public static AuthSession create(
            String sessionId,
            Long memberId,
            String deviceId,
            String deviceName,
            String userAgent,
            String refreshTokenJti,
            String refreshTokenHash,
            LocalDateTime expiresAt
    ) {
        return AuthSession.builder()
                .sessionId(sessionId)
                .memberId(memberId)
                .deviceId(deviceId)
                .deviceName(deviceName)
                .userAgent(userAgent)
                .refreshTokenJti(refreshTokenJti)
                .refreshTokenHash(refreshTokenHash)
                .status(AuthSessionStatus.ACTIVE)
                .expiresAt(expiresAt)
                .build();
    }

    public boolean isActive(LocalDateTime now) {
        return status == AuthSessionStatus.ACTIVE && expiresAt.isAfter(now);
    }

    public void rotateRefreshToken(String refreshTokenJti, String refreshTokenHash, LocalDateTime lastUsedAt) {
        this.refreshTokenJti = refreshTokenJti;
        this.refreshTokenHash = refreshTokenHash;
        this.lastUsedAt = lastUsedAt;
    }

    public void logout(LocalDateTime loggedOutAt) {
        this.status = AuthSessionStatus.LOGGED_OUT;
        this.loggedOutAt = loggedOutAt;
    }

    public void revoke() {
        this.status = AuthSessionStatus.REVOKED;
    }

    public void expire() {
        this.status = AuthSessionStatus.EXPIRED;
    }
}
