package com.very.relink.notification.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

public record WebPushSubscriptionDeleteRequest(
        @NotBlank String endpoint
) {
}
