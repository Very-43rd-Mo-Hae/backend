package com.very.relink.notification.presentation.controller;

import com.very.relink.core.presentation.RestResponse;
import com.very.relink.notification.adapter.in.web.WebPushPublicKeyResponse;
import com.very.relink.notification.adapter.in.web.WebPushSubscriptionDeleteRequest;
import com.very.relink.notification.adapter.in.web.WebPushSubscriptionRequest;
import com.very.relink.notification.application.command.DisableWebPushSubscriptionCommand;
import com.very.relink.notification.application.command.RegisterWebPushSubscriptionCommand;
import com.very.relink.notification.application.port.in.CurrentUserProvider;
import com.very.relink.notification.application.service.WebPushNotificationService;
import com.very.relink.notification.infrastructure.config.NotificationProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/push-subscriptions")
public class WebPushSubscriptionController {

    private final WebPushNotificationService webPushNotificationService;
    private final CurrentUserProvider currentUserProvider;
    private final NotificationProperties notificationProperties;

    @PostMapping
    public ResponseEntity<RestResponse<Void>> register(
            @Valid @RequestBody WebPushSubscriptionRequest request,
            @RequestHeader(value = "User-Agent", required = false) String userAgent
    ) {
        Long userId = currentUserProvider.getCurrentUserId();
        webPushNotificationService.register(new RegisterWebPushSubscriptionCommand(
                userId,
                request.endpoint(),
                request.p256dh(),
                request.auth(),
                userAgent
        ));

        return ResponseEntity.ok(new RestResponse<>(null));
    }

    @DeleteMapping
    public ResponseEntity<RestResponse<Void>> disable(
            @Valid @RequestBody WebPushSubscriptionDeleteRequest request
    ) {
        Long userId = currentUserProvider.getCurrentUserId();
        webPushNotificationService.disable(new DisableWebPushSubscriptionCommand(userId, request.endpoint()));
        return ResponseEntity.ok(new RestResponse<>(null));
    }

    @GetMapping("/public-key")
    public ResponseEntity<RestResponse<WebPushPublicKeyResponse>> getPublicKey() {
        return ResponseEntity.ok(new RestResponse<>(
                new WebPushPublicKeyResponse(notificationProperties.webPush().publicKey())
        ));
    }
}
