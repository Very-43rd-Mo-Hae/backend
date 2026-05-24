package com.very.relink.auth.adapter.out.social.kakao;

import com.very.relink.auth.application.result.SocialLoginUserInfo;
import com.very.relink.auth.domain.value.OAuth2Provider;
import com.very.relink.auth.exception.AuthErrorCode;
import org.springframework.stereotype.Component;

@Component
public class KakaoUserInfoMapper {

    public SocialLoginUserInfo toSocialLoginUserInfo(KakaoUserMeResponse response) {
        if (response == null || response.id() == null) {
            throw AuthErrorCode.OAUTH2_LOGIN_FAILED.toException();
        }

        KakaoAccountResponse kakaoAccount = response.kakaoAccount();
        KakaoProfileResponse profile = kakaoAccount == null ? null : kakaoAccount.profile();

        return new SocialLoginUserInfo(
                OAuth2Provider.KAKAO,
                String.valueOf(response.id()),
                kakaoAccount == null ? null : kakaoAccount.email(),
                profile == null ? null : profile.nickname(),
                profile == null ? null : profile.profileImageUrl()
        );
    }
}
