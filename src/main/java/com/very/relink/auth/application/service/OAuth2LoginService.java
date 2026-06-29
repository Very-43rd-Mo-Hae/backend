package com.very.relink.auth.application.service;

import com.very.relink.auth.application.command.OAuth2LoginCommand;
import com.very.relink.auth.application.port.in.OAuth2LoginUseCase;
import com.very.relink.auth.application.port.out.RefreshTokenHashPort;
import com.very.relink.auth.application.port.out.SaveAuthSessionPort;
import com.very.relink.auth.application.port.out.SaveRefreshTokenCachePort;
import com.very.relink.auth.application.port.out.TokenIssuePort;
import com.very.relink.auth.application.result.OAuth2LoginResult;
import com.very.relink.auth.domain.session.AuthSession;
import com.very.relink.auth.domain.token.AuthTokens;
import com.very.relink.auth.exception.AuthErrorCode;
import com.very.relink.member.application.port.out.LoadMemberPort;
import com.very.relink.member.application.port.out.SaveMemberPort;
import com.very.relink.member.domain.Member;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2LoginService implements OAuth2LoginUseCase {

    private static final String TEMPORARY_NICKNAME_PREFIX = "temp-nickname-";

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final TokenIssuePort tokenIssuePort;
    private final SaveAuthSessionPort saveAuthSessionPort;
    private final RefreshTokenHashPort refreshTokenHashPort;
    private final SaveRefreshTokenCachePort saveRefreshTokenCachePort;

    @Override
    @Transactional
    public OAuth2LoginResult login(OAuth2LoginCommand oAuth2LoginCommand) {
        String providerId = oAuth2LoginCommand.providerId();
        if (providerId == null || providerId.isEmpty()) {
            throw AuthErrorCode.OAUTH2_LOGIN_FAILED.toException();
        }

        Member member = loadMemberPort.findByProviderAndProviderId(
                        oAuth2LoginCommand.provider(),
                        providerId
                )
                .orElseGet(() -> saveMemberPort.save(
                        Member.create(
                                oAuth2LoginCommand.email(),
                                resolveName(oAuth2LoginCommand.name()),
                                oAuth2LoginCommand.imageUrl(),
                                oAuth2LoginCommand.provider(),
                                providerId
                        )
                ));

        String sessionId = UUID.randomUUID().toString();
        String refreshTokenJti = UUID.randomUUID().toString();

        AuthTokens authTokens = tokenIssuePort.issue(member, sessionId, refreshTokenJti);
        String refreshTokenHash = refreshTokenHashPort.hash(authTokens.refreshToken());
        Duration refreshTokenTtl = Duration.ofSeconds(authTokens.refreshTokenExpiresIn());

        AuthSession authSession = AuthSession.create(
                sessionId,
                member.getId(),
                oAuth2LoginCommand.deviceId(),
                oAuth2LoginCommand.deviceName(),
                oAuth2LoginCommand.userAgent(),
                refreshTokenJti,
                refreshTokenHash,
                LocalDateTime.now().plus(refreshTokenTtl)
        );
        saveAuthSessionPort.save(authSession);
        saveRefreshTokenCachePort.save(sessionId, refreshTokenHash, refreshTokenTtl);

        return new OAuth2LoginResult(member.getId(), authTokens);
    }

    private String resolveName(String name) {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return TEMPORARY_NICKNAME_PREFIX + UUID.randomUUID();
    }
}
