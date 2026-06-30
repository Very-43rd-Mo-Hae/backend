package com.very.relink.notification.presentation.controller;

import com.very.relink.core.presentation.RestResponse;
import com.very.relink.notification.application.port.in.CurrentUserProvider;
import com.very.relink.notification.application.response.NotificationInboxResponses.NotificationInboxResponse;
import com.very.relink.notification.application.service.NotificationInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationInboxController {

    private final CurrentUserProvider currentUserProvider;
    private final NotificationInboxService notificationInboxService;

    @GetMapping
    public ResponseEntity<RestResponse<NotificationInboxResponse>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long memberId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(notificationInboxService.getNotifications(memberId, page, size)));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<RestResponse<Void>> markAllRead() {
        Long memberId = currentUserProvider.getCurrentUserId();
        notificationInboxService.markAllRead(memberId);
        return ResponseEntity.ok(new RestResponse<>(null));
    }

    @DeleteMapping("/{notificationInboxItemId}")
    public ResponseEntity<RestResponse<Void>> deleteNotification(@PathVariable Long notificationInboxItemId) {
        Long memberId = currentUserProvider.getCurrentUserId();
        notificationInboxService.deleteNotification(memberId, notificationInboxItemId);
        return ResponseEntity.ok(new RestResponse<>(null));
    }
}
