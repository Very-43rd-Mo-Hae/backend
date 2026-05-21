package com.very.relink.auth.adapter.in.web;

import com.very.relink.auth.domain.value.OAuth2Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "소셜 로그인 요청")
public record SocialLoginRequest(
        @Schema(description = "소셜 로그인 제공자", example = "GOOGLE")
        @NotNull
        OAuth2Provider provider,

        @Schema(
                description = "Google, Apple SDK에서 받은 ID Token. Kakao 로그인에서는 사용하지 않습니다.",
                example = "eyJraWQiOiJ..."
        )
        String idToken,

        @Schema(
                description = "Kakao SDK에서 받은 Access Token. Google, Apple 로그인에서는 사용하지 않습니다.",
                example = "kakao-access-token"
        )
        String accessToken,

        @Schema(
                description = "Apple 최초 로그인 시 앱에서 받은 사용자 이름. 다른 provider에서는 선택값입니다.",
                example = "홍길동"
        )
        String name
) {
}
