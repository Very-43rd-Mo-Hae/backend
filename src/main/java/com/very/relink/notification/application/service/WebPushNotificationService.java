package com.very.relink.notification.application.service;

import com.very.relink.notification.application.command.DisableWebPushSubscriptionCommand;
import com.very.relink.notification.application.command.RegisterWebPushSubscriptionCommand;
import com.very.relink.notification.application.command.SendWebPushNotificationCommand;
import com.very.relink.notification.application.dto.NotificationTargetProjection;
import com.very.relink.notification.application.dto.WebPushPayload;
import com.very.relink.notification.application.dto.WebPushSendResult;
import com.very.relink.notification.application.port.out.NotificationSendDeduplicationPort;
import com.very.relink.notification.application.port.out.NotificationTargetCommandPort;
import com.very.relink.notification.application.port.out.NotificationTargetQueryPort;
import com.very.relink.notification.application.port.out.WebPushSenderPort;
import com.very.relink.notification.domain.model.NotificationChannel;
import com.very.relink.notification.domain.model.NotificationTarget;
import com.very.relink.notification.domain.model.PushProvider;
import com.very.relink.notification.infrastructure.config.NotificationProperties;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WebPushNotificationService {

    private final NotificationTargetCommandPort notificationTargetCommandPort;
    private final NotificationTargetQueryPort notificationTargetQueryPort;
    private final NotificationSendDeduplicationPort notificationSendDeduplicationPort;
    private final WebPushSenderPort webPushSenderPort;
    private final NotificationProperties notificationProperties;

    @Transactional
    public void register(RegisterWebPushSubscriptionCommand command) {
        PushProvider provider = PushProvider.fromUserAgent(command.userAgent());

        NotificationTarget target = notificationTargetCommandPort.findByEndpoint(command.endpoint())
                .orElseGet(() -> NotificationTarget.createWebPush(
                        command.userId(),
                        command.endpoint(),
                        command.p256dh(),
                        command.auth(),
                        command.userAgent(),
                        provider
                ));

        target.refreshSubscription(
                command.userId(),
                command.p256dh(),
                command.auth(),
                command.userAgent(),
                provider
        );

        notificationTargetCommandPort.save(target);
    }

    @Transactional
    public void disable(DisableWebPushSubscriptionCommand command) {
        notificationTargetCommandPort.disableByEndpoint(command.userId(), command.endpoint());
    }

    public WebPushSendResult send(SendWebPushNotificationCommand command) {
        if (command.notificationId() != null) {
            Duration ttl = Duration.ofSeconds(notificationProperties.redis().dedupTtlSeconds());
            boolean acquired = notificationSendDeduplicationPort.acquireSendLock(
                    command.userId(),
                    command.notificationId(),
                    NotificationChannel.WEB_PUSH,
                    ttl
            );
            if (!acquired) {
                return WebPushSendResult.skipped();
            }
        }

        List<NotificationTargetProjection> targets = notificationTargetQueryPort.findActiveTargetsByUserId(command.userId());
        WebPushPayload payload = new WebPushPayload(
                command.title(),
                command.body(),
                command.linkUrl(),
                command.notificationId(),
                mergeLinkUrl(command.data(), command.linkUrl())
        );

        int success = 0;
        int failure = 0;
        int expired = 0;

        for (NotificationTargetProjection target : targets) {
            WebPushSenderPort.WebPushSendResponse response = webPushSenderPort.send(target, payload);
            if (response.success()) {
                notificationTargetCommandPort.markUsed(target.id(), LocalDateTime.now());
                success++;
                continue;
            }

            failure++;
            if (response.expired()) {
                notificationTargetCommandPort.expireByEndpoint(target.endpoint());
                expired++;
            }
        }

        return WebPushSendResult.builder()
                .total(targets.size())
                .success(success)
                .failure(failure)
                .expired(expired)
                .skippedByDeduplication(false)
                .build();
    }

    private Map<String, Object> mergeLinkUrl(Map<String, Object> data, String linkUrl) {
        Map<String, Object> merged = new HashMap<>();
        if (data != null) {
            merged.putAll(data);
        }
        if (linkUrl != null) {
            merged.put("linkUrl", linkUrl);
        }
        return merged;
    }
}
