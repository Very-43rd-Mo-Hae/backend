package com.very.relink.auth.application.service;

import com.very.relink.auth.application.port.out.TokenIssuePort;
import com.very.relink.auth.application.port.out.OAuth2UserClientPort;
import com.very.relink.auth.domain.oauth2.userinfo.AppleOAuth2UserInfo;
import com.very.relink.auth.domain.oauth2.userinfo.GoogleOAuth2UserInfo;
import com.very.relink.auth.domain.oauth2.userinfo.KakaoOAuth2UserInfo;
import com.very.relink.auth.domain.oauth2.userinfo.OAuth2UserInfo;
import com.very.relink.auth.domain.token.AuthTokens;
import com.very.relink.auth.domain.value.OAuth2Provider;
import com.very.relink.auth.exception.AuthErrorCode;
import com.very.relink.member.application.port.out.LoadMemberPort;
import com.very.relink.member.application.port.out.SaveMemberPort;
import com.very.relink.member.domain.Member;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final OAuth2UserClientPort oAuth2UserClientPort;
    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final TokenIssuePort tokenIssuePort;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = oAuth2UserClientPort.loadUser(userRequest);
        OAuth2Provider provider = extractProvider(userRequest);
        OAuth2UserInfo userInfo = createOAuth2UserInfo(provider, oAuth2User.getAttributes());

        return login(provider, userInfo, oAuth2User);
    }

    private OAuth2Provider extractProvider(OAuth2UserRequest userRequest) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        try {
            return OAuth2Provider.fromRegistrationId(registrationId);
        } catch (IllegalArgumentException ignored) {
            throw AuthErrorCode.UNSUPPORTED_OAUTH2_PROVIDER.toException();
        }
    }

    private OAuth2UserInfo createOAuth2UserInfo(
            OAuth2Provider provider,
            Map<String, Object> attributes
    ) {
        return switch (provider) {
            case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
            case KAKAO -> new KakaoOAuth2UserInfo(attributes);
            case APPLE -> new AppleOAuth2UserInfo(attributes);
        };
    }

    private OAuth2User login(
            OAuth2Provider provider,
            OAuth2UserInfo userInfo,
            OAuth2User oAuth2User
    ) {
        return switch (provider) {
            case GOOGLE -> loginWithGoogle(userInfo, oAuth2User);
            case KAKAO -> loginWithKakao(userInfo, oAuth2User);
            case APPLE -> loginWithApple(userInfo, oAuth2User);
        };
    }

    private OAuth2User loginWithGoogle(OAuth2UserInfo userInfo, OAuth2User oAuth2User) {
        return loginWithOAuth2UserInfo(userInfo, oAuth2User, OAuth2Provider.GOOGLE);
    }

    private OAuth2User loginWithKakao(OAuth2UserInfo userInfo, OAuth2User oAuth2User) {
        return loginWithOAuth2UserInfo(userInfo, oAuth2User, OAuth2Provider.KAKAO);
    }

    private OAuth2User loginWithApple(OAuth2UserInfo userInfo, OAuth2User oAuth2User) {
        return loginWithOAuth2UserInfo(userInfo, oAuth2User, OAuth2Provider.APPLE);
    }

    private OAuth2User loginWithOAuth2UserInfo(
            OAuth2UserInfo userInfo,
            OAuth2User oAuth2User,
            OAuth2Provider provider
    ) {
        String email = userInfo.getEmail();
        if (email == null || email.isBlank()) {
            throw AuthErrorCode.OAUTH2_EMAIL_NOT_FOUND.toException();
        }

        Member member = loadMemberPort.findByEmail(email)
                .orElseGet(() -> saveMemberPort.save(Member.create(
                        email,
                        userInfo.getName(),
                        userInfo.getImageUrl(),
                        provider
                )));

        AuthTokens authTokens = tokenIssuePort.issue(member);
        return new OAuth2AuthenticatedUser(oAuth2User, member.getId(), authTokens);
    }
}
