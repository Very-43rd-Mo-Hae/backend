package com.very.relink.auth.adapter.out.social.kakao;

public record KakaoUserMeResponse(
        Long id,
        KakaoAccountResponse kakao_account
) {

    public KakaoAccountResponse kakaoAccount() {
        return kakao_account;
    }
}
