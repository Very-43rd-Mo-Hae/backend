package com.very.relink.auth.application.result;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReissueTokenResponse(
        @Schema(
                description = "새로 발급된 AccessToken",
                example = "new-access.jwt..."
        )
        String accessToken,

        @Schema(
                description = "새로 발급된 RefreshToken",
                example = "new-refresh.jwt..."
        )
        String refreshToken,

        @Schema(
                description = "새로 발급된 AccessToken 만료시간",
                example = "1800"
        )
        long accessTokenExpiresIn,

        @Schema(
                description = "새로 발급된 RefreshToken 만료시간",
                example = "1209600"
        )
        long refreshTokenExpiresIn
) {
}
