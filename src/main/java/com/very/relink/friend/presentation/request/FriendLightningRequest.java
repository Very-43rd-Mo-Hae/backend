package com.very.relink.friend.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

public record FriendLightningRequest(
        @Schema(
                description = "번개 가능 종료 시각. ISO-8601 offset datetime.",
                example = "2026-06-30T20:30:00+09:00"
        )
        OffsetDateTime expiresAt
) {
}
