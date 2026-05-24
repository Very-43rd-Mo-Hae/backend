package com.very.relink.auth.adapter.out.social.kakao;

public record KakaoProfileResponse(
        String nickname,
        String profile_image_url
) {

    public String profileImageUrl() {
        return profile_image_url;
    }
}
