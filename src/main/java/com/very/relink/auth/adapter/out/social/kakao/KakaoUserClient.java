package com.very.relink.auth.adapter.out.social.kakao;

public interface KakaoUserClient {

    KakaoUserMeResponse fetchUserMe(String accessToken);
}
