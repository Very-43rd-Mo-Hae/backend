package com.very.relink.auth.adapter.out.social.kakao;

import com.very.relink.auth.exception.AuthErrorCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class KakaoUserRestClient implements KakaoUserClient {

    private final RestTemplate restTemplate;
    private final KakaoSocialProperties kakaoSocialProperties;

    @Autowired
    public KakaoUserRestClient(KakaoSocialProperties kakaoSocialProperties) {
        this(new RestTemplate(), kakaoSocialProperties);
    }

    KakaoUserRestClient(
            RestTemplate restTemplate,
            KakaoSocialProperties kakaoSocialProperties
    ) {
        this.restTemplate = restTemplate;
        this.kakaoSocialProperties = kakaoSocialProperties;
    }

    @Override
    public KakaoUserMeResponse fetchUserMe(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        try {
            return restTemplate.exchange(
                    kakaoSocialProperties.userInfoUri(),
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    KakaoUserMeResponse.class
            ).getBody();
        } catch (RestClientException e) {
            throw AuthErrorCode.OAUTH2_LOGIN_FAILED.toException();
        }
    }
}
