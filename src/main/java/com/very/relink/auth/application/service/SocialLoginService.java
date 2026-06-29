package com.very.relink.auth.application.service;

import com.very.relink.auth.application.command.OAuth2LoginCommand;
import com.very.relink.auth.application.command.SocialLoginCommand;
import com.very.relink.auth.application.port.in.OAuth2LoginUseCase;
import com.very.relink.auth.application.port.in.SocialLoginUseCase;
import com.very.relink.auth.application.port.out.SocialUserResolver;
import com.very.relink.auth.application.result.OAuth2LoginResult;
import com.very.relink.auth.application.result.SocialLoginUserInfo;
import com.very.relink.auth.domain.value.OAuth2Provider;
import com.very.relink.auth.exception.AuthErrorCode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SocialLoginService implements SocialLoginUseCase {

    private final OAuth2LoginUseCase oAuth2LoginUseCase;
    private final Map<OAuth2Provider, SocialUserResolver> resolvers;

    public SocialLoginService(
            OAuth2LoginUseCase oAuth2LoginUseCase,
            List<SocialUserResolver> resolvers
    ) {
        this.oAuth2LoginUseCase = oAuth2LoginUseCase;
        this.resolvers = resolvers.stream()
                .collect(Collectors.toMap(
                                SocialUserResolver::supports, Function.identity()
                        )
                );
    }

    @Override
    public OAuth2LoginResult login(SocialLoginCommand socialLoginCommand) {
        SocialUserResolver resolver = resolvers.get(socialLoginCommand.provider());

        if(resolver == null) {
            throw AuthErrorCode.UNSUPPORTED_OAUTH2_PROVIDER.toException();
        }

        SocialLoginUserInfo userInfo = resolver.resolve(socialLoginCommand);
        String name = resolveName(socialLoginCommand.name(), userInfo.name());

        return oAuth2LoginUseCase.login(new OAuth2LoginCommand(
                userInfo.oAuth2Provider(),
                userInfo.providerId(),
                userInfo.email(),
                name,
                userInfo.imageUrl(),
                socialLoginCommand.deviceId(),
                socialLoginCommand.deviceName(),
                socialLoginCommand.userAgent()
        ));
    }

    private String resolveName(String requestedName, String socialName) {
        if (requestedName != null && !requestedName.isBlank()) {
            return requestedName;
        }
        return socialName;
    }
}
