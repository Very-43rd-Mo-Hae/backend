package com.very.relink.auth.adapter.in.token;

import io.swagger.v3.oas.annotations.media.Schema;

public record LogoutRequest(
        @Schema(
                description = "Current session refresh token",
                example = "current-refresh.jwt..."
        )
        String refreshToken
) {
}
