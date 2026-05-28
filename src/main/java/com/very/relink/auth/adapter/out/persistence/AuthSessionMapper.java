package com.very.relink.auth.adapter.out.persistence;

import com.very.relink.auth.domain.session.AuthSession;
import com.very.relink.member.adapter.out.persistence.MemberJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AuthSessionMapper {

    public AuthSession toDomain(AuthSessionJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return AuthSession.builder()
                .id(entity.getId())
                .sessionId(entity.getSessionId())
                .memberId(entity.getMember().getId())
                .deviceId(entity.getDeviceId())
                .deviceName(entity.getDeviceName())
                .userAgent(entity.getUserAgent())
                .refreshTokenJti(entity.getRefreshTokenJti())
                .refreshTokenHash(entity.getRefreshTokenHash())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .lastUsedAt(entity.getLastUsedAt())
                .expiresAt(entity.getExpiresAt())
                .loggedOutAt(entity.getLoggedOutAt())
                .build();
    }

    public AuthSessionJpaEntity toEntity(AuthSession authSession, MemberJpaEntity member) {
        if (authSession == null) {
            return null;
        }

        return AuthSessionJpaEntity.builder()
                .id(authSession.getId())
                .sessionId(authSession.getSessionId())
                .member(member)
                .deviceId(authSession.getDeviceId())
                .deviceName(authSession.getDeviceName())
                .userAgent(authSession.getUserAgent())
                .refreshTokenJti(authSession.getRefreshTokenJti())
                .refreshTokenHash(authSession.getRefreshTokenHash())
                .status(authSession.getStatus())
                .createdAt(authSession.getCreatedAt())
                .lastUsedAt(authSession.getLastUsedAt())
                .expiresAt(authSession.getExpiresAt())
                .loggedOutAt(authSession.getLoggedOutAt())
                .build();
    }
}
