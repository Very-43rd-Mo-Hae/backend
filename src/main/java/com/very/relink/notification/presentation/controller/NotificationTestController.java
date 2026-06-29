package com.very.relink.notification.presentation.controller;

import com.very.relink.core.presentation.RestResponse;
import com.very.relink.notification.adapter.in.web.TestWebPushNotificationRequest;
import com.very.relink.notification.application.command.SendWebPushNotificationCommand;
import com.very.relink.notification.application.dto.WebPushSendResult;
import com.very.relink.notification.application.port.in.CurrentUserProvider;
import com.very.relink.notification.application.service.WebPushNotificationService;
import com.very.relink.notification.presentation.swagger.NotificationTestSwagger;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationTestController implements NotificationTestSwagger {

    private final WebPushNotificationService webPushNotificationService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping("/test")
    @Override
    public ResponseEntity<RestResponse<WebPushSendResult>> sendTest(
            @Valid @RequestBody TestWebPushNotificationRequest request
    ) {
        Long userId = currentUserProvider.getCurrentUserId();
        WebPushSendResult result = webPushNotificationService.send(new SendWebPushNotificationCommand(
                userId,
                request.title(),
                request.body(),
                request.linkUrl(),
                request.notificationId(),
                request.data()
        ));

        return ResponseEntity.ok(new RestResponse<>(result));
    }
}
