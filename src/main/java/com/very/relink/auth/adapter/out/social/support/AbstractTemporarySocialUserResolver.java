package com.very.relink.auth.adapter.out.social.support;

import com.very.relink.auth.application.command.SocialLoginCommand;
import com.very.relink.auth.application.port.out.SocialUserResolver;
import com.very.relink.auth.application.result.SocialLoginUserInfo;
import com.very.relink.auth.domain.value.OAuth2Provider;
import com.very.relink.auth.exception.AuthErrorCode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public abstract class AbstractTemporarySocialUserResolver implements SocialUserResolver {

    @Override
    public SocialLoginUserInfo resolve(SocialLoginCommand socialLoginCommand) {
        String token = resolveToken(socialLoginCommand);
        if (token == null || token.isBlank()) {
            throw AuthErrorCode.OAUTH2_LOGIN_FAILED.toException();
        }

        OAuth2Provider provider = supports();
        String providerName = provider.name().toLowerCase();
        String providerId = providerName + "-temp-" + sha256(token);

        return new SocialLoginUserInfo(
                provider,
                providerId,
                providerId + "@temporary.relink",
                resolveName(socialLoginCommand, providerName),
                null
        );
    }

    protected abstract String resolveToken(SocialLoginCommand socialLoginCommand);

    private String resolveName(SocialLoginCommand socialLoginCommand, String providerName) {
        if (socialLoginCommand.name() != null && !socialLoginCommand.name().isBlank()) {
            return socialLoginCommand.name();
        }

        return providerName + "-temporary-user";
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw AuthErrorCode.OAUTH2_LOGIN_FAILED.toException();
        }
    }
}
