package com.very.relink.auth.adapter.out.social.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.very.relink.auth.application.command.SocialLoginCommand;
import com.very.relink.auth.application.result.SocialLoginUserInfo;
import com.very.relink.auth.domain.value.OAuth2Provider;
import com.very.relink.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KakaoSocialUserResolverTest {

    @Test
    @DisplayName("카카오 access token으로 사용자 정보를 조회하고 매핑한다")
    void resolve() {
        KakaoSocialUserResolver resolver = new KakaoSocialUserResolver(
                accessToken -> new KakaoUserMeResponse(
                        123456789L,
                        new KakaoAccountResponse(
                                "kakao@example.com",
                                new KakaoProfileResponse(
                                        "카카오유저",
                                        "https://example.com/kakao-profile.png"
                                )
                        )
                ),
                new KakaoUserInfoMapper()
        );

        SocialLoginUserInfo userInfo = resolver.resolve(new SocialLoginCommand(
                OAuth2Provider.KAKAO,
                null,
                "kakao-access-token",
                null,
                null,
                null,
                null,
                false
        ));

        assertThat(userInfo.oAuth2Provider()).isEqualTo(OAuth2Provider.KAKAO);
        assertThat(userInfo.providerId()).isEqualTo("123456789");
        assertThat(userInfo.email()).isEqualTo("kakao@example.com");
        assertThat(userInfo.name()).isEqualTo("카카오유저");
        assertThat(userInfo.imageUrl()).isEqualTo("https://example.com/kakao-profile.png");
    }

    @Test
    @DisplayName("카카오 access token이 비어 있으면 로그인에 실패한다")
    void failWhenAccessTokenIsBlank() {
        KakaoSocialUserResolver resolver = new KakaoSocialUserResolver(
                accessToken -> null,
                new KakaoUserInfoMapper()
        );

        assertThatThrownBy(() -> resolver.resolve(new SocialLoginCommand(
                OAuth2Provider.KAKAO,
                null,
                " ",
                null,
                null,
                null,
                null,
                false
        )))
                .isInstanceOf(DomainException.class)
                .hasMessage("OAuth2 로그인에 실패했습니다.");
    }
}
