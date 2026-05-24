package com.very.relink.auth.adapter.out.social.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.very.relink.auth.application.result.SocialLoginUserInfo;
import com.very.relink.auth.domain.value.OAuth2Provider;
import com.very.relink.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class KakaoUserInfoMapperTest {

    private final KakaoUserInfoMapper mapper = new KakaoUserInfoMapper();

    @Test
    @DisplayName("카카오 사용자 정보 응답을 소셜 로그인 사용자 정보로 변환한다")
    void toSocialLoginUserInfo() {
        KakaoUserMeResponse response = new KakaoUserMeResponse(
                123456789L,
                new KakaoAccountResponse(
                        "kakao@example.com",
                        new KakaoProfileResponse(
                                "카카오유저",
                                "https://example.com/kakao-profile.png"
                        )
                )
        );

        SocialLoginUserInfo userInfo = mapper.toSocialLoginUserInfo(response);

        assertThat(userInfo.oAuth2Provider()).isEqualTo(OAuth2Provider.KAKAO);
        assertThat(userInfo.providerId()).isEqualTo("123456789");
        assertThat(userInfo.email()).isEqualTo("kakao@example.com");
        assertThat(userInfo.name()).isEqualTo("카카오유저");
        assertThat(userInfo.imageUrl()).isEqualTo("https://example.com/kakao-profile.png");
    }

    @Test
    @DisplayName("카카오 provider id가 없으면 로그인에 실패한다")
    void failWhenProviderIdDoesNotExist() {
        assertThatThrownBy(() -> mapper.toSocialLoginUserInfo(new KakaoUserMeResponse(null, null)))
                .isInstanceOf(DomainException.class)
                .hasMessage("OAuth2 로그인에 실패했습니다.");
    }

    @Test
    @DisplayName("카카오 계정 정보가 없어도 provider id만 있으면 변환한다")
    void mapWithoutKakaoAccount() {
        SocialLoginUserInfo userInfo = mapper.toSocialLoginUserInfo(
                new KakaoUserMeResponse(123456789L, null)
        );

        assertThat(userInfo.providerId()).isEqualTo("123456789");
        assertThat(userInfo.email()).isNull();
        assertThat(userInfo.name()).isNull();
        assertThat(userInfo.imageUrl()).isNull();
    }
}
