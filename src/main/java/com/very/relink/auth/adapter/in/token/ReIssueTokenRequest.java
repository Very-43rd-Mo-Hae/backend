package com.very.relink.auth.adapter.in.token;

import io.swagger.v3.oas.annotations.media.Schema;

public record ReIssueTokenRequest(
        @Schema(
                description = "현재 세션 사용자의 RefreshToken",
                example = "current-refresh.jwt..."
        )
        String refreshToken
) {
}
