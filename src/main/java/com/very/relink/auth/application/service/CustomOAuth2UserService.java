package com.very.relink.auth.application.service;

import com.very.relink.auth.application.port.in.OAuth2LoginUseCase;
import com.very.relink.auth.application.port.out.OAuth2UserClientPort;
import com.very.relink.auth.application.result.OAuth2LoginResult;
import com.very.relink.auth.application.command.OAuth2LoginCommand;
import com.very.relink.auth.domain.oauth2.userinfo.OAuth2UserInfo;
import com.very.relink.auth.domain.value.OAuth2Provider;
import com.very.relink.auth.exception.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2UserClientPort oAuth2UserClientPort;
    private final OAuth2LoginUseCase oAuth2LoginUseCase;
    private final OAuth2UserInfoFactory oAuth2UserInfoFactory;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = oAuth2UserClientPort.loadUser(userRequest);
        OAuth2Provider oAuth2Provider = extractProvider(userRequest);
        OAuth2UserInfo oAuth2UserInfo = oAuth2UserInfoFactory.create(oAuth2Provider, oAuth2User.getAttributes());

        OAuth2LoginResult loginResult = oAuth2LoginUseCase.login(new OAuth2LoginCommand(
                oAuth2Provider,
                oAuth2UserInfo.getEmail(),
                oAuth2UserInfo.getName(),
                oAuth2UserInfo.getImageUrl()
        ));

        return new OAuth2AuthenticatedUser(
                oAuth2User,
                loginResult.memberId(),
                loginResult.authTokens()
        );
    }

    private OAuth2Provider extractProvider(OAuth2UserRequest userRequest) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        try {
            return OAuth2Provider.fromRegistrationId(registrationId);
        } catch (IllegalArgumentException ignored) {
            throw AuthErrorCode.UNSUPPORTED_OAUTH2_PROVIDER.toException();
        }
    }
}
