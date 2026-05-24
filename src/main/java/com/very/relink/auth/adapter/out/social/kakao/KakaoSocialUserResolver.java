package com.very.relink.auth.adapter.out.social.kakao;

import com.very.relink.auth.application.command.SocialLoginCommand;
import com.very.relink.auth.application.port.out.SocialUserResolver;
import com.very.relink.auth.application.result.SocialLoginUserInfo;
import com.very.relink.auth.domain.value.OAuth2Provider;
import com.very.relink.auth.exception.AuthErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoSocialUserResolver implements SocialUserResolver {

    private final KakaoUserClient kakaoUserClient;
    private final KakaoUserInfoMapper kakaoUserInfoMapper;

    @Override
    public OAuth2Provider supports() {
        return OAuth2Provider.KAKAO;
    }

    @Override
    public SocialLoginUserInfo resolve(SocialLoginCommand socialLoginCommand) {
        String accessToken = socialLoginCommand.accessToken();
        if (accessToken == null || accessToken.isBlank()) {
            throw AuthErrorCode.OAUTH2_LOGIN_FAILED.toException();
        }

        KakaoUserMeResponse response = kakaoUserClient.fetchUserMe(accessToken);
        return kakaoUserInfoMapper.toSocialLoginUserInfo(response);
    }
}
