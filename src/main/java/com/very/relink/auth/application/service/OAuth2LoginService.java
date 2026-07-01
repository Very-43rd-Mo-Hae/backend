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
import java.util.Optional;
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

        Optional<Member> existingMember = loadMemberPort.findByProviderAndProviderId(
                oAuth2LoginCommand.provider(),
                providerId
        );

        if (existingMember.isPresent() && shouldRequireAccountRestore(existingMember.get(), oAuth2LoginCommand.restoreAccount())) {
            return OAuth2LoginResult.requiresAccountRestore(existingMember.get().getId());
        }

        Member member = existingMember
                .map(memberToLogin -> restoreIfRequested(memberToLogin, oAuth2LoginCommand.restoreAccount()))
                .map(memberToLogin -> updateEmailIfBlank(memberToLogin, oAuth2LoginCommand.email()))
                .orElseGet(() -> saveMemberPort.save(Member.create(
                        oAuth2LoginCommand.email(),
                        resolveName(oAuth2LoginCommand.name()),
                        oAuth2LoginCommand.imageUrl(),
                        oAuth2LoginCommand.provider(),
                        providerId
                )));

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

        return OAuth2LoginResult.loggedIn(member.getId(), authTokens);
    }

    private boolean shouldRequireAccountRestore(Member member, boolean restoreAccount) {
        return member.isWithdrawn() && member.canRestore(LocalDateTime.now()) && !restoreAccount;
    }

    private Member restoreIfRequested(Member member, boolean restoreAccount) {
        if (!member.isWithdrawn()) {
            return member;
        }
        if (!member.canRestore(LocalDateTime.now())) {
            throw AuthErrorCode.OAUTH2_LOGIN_FAILED.toException();
        }
        member.restore();
        return saveMemberPort.save(member);
    }

    private Member updateEmailIfBlank(Member member, String email) {
        if (member.updateEmailIfBlank(email)) {
            return saveMemberPort.save(member);
        }
        return member;
    }

    private String resolveName(String name) {
        if (name != null && !name.isBlank()) {
            return name;
        }
        return TEMPORARY_NICKNAME_PREFIX + UUID.randomUUID();
    }
}
