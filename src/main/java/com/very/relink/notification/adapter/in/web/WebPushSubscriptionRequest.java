package com.very.relink.notification.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record WebPushSubscriptionRequest(
        @NotBlank String endpoint,
        @NotBlank String p256dh,
        @NotBlank String auth
) {
}
