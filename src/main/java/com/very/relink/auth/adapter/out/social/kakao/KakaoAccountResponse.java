package com.very.relink.auth.adapter.out.social.kakao;

public record KakaoAccountResponse(
        String email,
        KakaoProfileResponse profile
) {
}
