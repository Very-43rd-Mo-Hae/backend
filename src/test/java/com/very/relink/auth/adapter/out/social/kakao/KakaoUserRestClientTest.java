package com.very.relink.auth.adapter.out.social.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.very.relink.core.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class KakaoUserRestClientTest {

    private static final String USER_INFO_URI = "https://kapi.kakao.com/v2/user/me";

    @Test
    @DisplayName("카카오 사용자 정보 API를 Bearer 토큰으로 호출한다")
    void fetchUserMe() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        KakaoUserRestClient client = new KakaoUserRestClient(
                restTemplate,
                new KakaoSocialProperties(USER_INFO_URI)
        );

        server.expect(requestTo(USER_INFO_URI))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer kakao-access-token"))
                .andRespond(withSuccess("""
                        {
                          "id": 123456789,
                          "kakao_account": {
                            "email": "kakao@example.com",
                            "profile": {
                              "nickname": "카카오유저",
                              "profile_image_url": "https://example.com/kakao-profile.png"
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        KakaoUserMeResponse response = client.fetchUserMe("kakao-access-token");

        assertThat(response.id()).isEqualTo(123456789L);
        assertThat(response.kakaoAccount().email()).isEqualTo("kakao@example.com");
        assertThat(response.kakaoAccount().profile().nickname()).isEqualTo("카카오유저");
        assertThat(response.kakaoAccount().profile().profileImageUrl())
                .isEqualTo("https://example.com/kakao-profile.png");
        server.verify();
    }

    @Test
    @DisplayName("카카오 사용자 정보 API 호출이 실패하면 로그인에 실패한다")
    void failWhenKakaoUserInfoRequestFails() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        KakaoUserRestClient client = new KakaoUserRestClient(
                restTemplate,
                new KakaoSocialProperties(USER_INFO_URI)
        );

        server.expect(requestTo(USER_INFO_URI))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.fetchUserMe("invalid-token"))
                .isInstanceOf(DomainException.class)
                .hasMessage("OAuth2 로그인에 실패했습니다.");
        server.verify();
    }
}
