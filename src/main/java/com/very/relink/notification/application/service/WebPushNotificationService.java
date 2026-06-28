package com.very.relink.notification.application.service;

import com.very.relink.notification.application.command.DisableWebPushSubscriptionCommand;
import com.very.relink.notification.application.command.RegisterWebPushSubscriptionCommand;
import com.very.relink.notification.application.command.SendWebPushNotificationCommand;
import com.very.relink.notification.application.dto.NotificationTargetProjection;
import com.very.relink.notification.application.dto.WebPushPayload;
import com.very.relink.notification.application.dto.WebPushSendResult;
import com.very.relink.notification.application.port.out.NotificationHistoryCommandPort;
import com.very.relink.notification.application.port.out.NotificationSendDeduplicationPort;
import com.very.relink.notification.application.port.out.NotificationTargetCommandPort;
import com.very.relink.notification.application.port.out.NotificationTargetQueryPort;
import com.very.relink.notification.application.port.out.WebPushSenderPort;
import com.very.relink.notification.domain.model.NotificationChannel;
import com.very.relink.notification.domain.model.NotificationDelivery;
import com.very.relink.notification.domain.model.NotificationDeliveryStatus;
import com.very.relink.notification.domain.model.NotificationMessage;
import com.very.relink.notification.domain.model.NotificationOutbox;
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
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class WebPushNotificationService {

    private final NotificationTargetCommandPort notificationTargetCommandPort;
    private final NotificationTargetQueryPort notificationTargetQueryPort;
    private final NotificationHistoryCommandPort notificationHistoryCommandPort;
    private final NotificationSendDeduplicationPort notificationSendDeduplicationPort;
    private final WebPushSenderPort webPushSenderPort;
    private final NotificationProperties notificationProperties;
    private final ObjectMapper objectMapper;

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

    @Transactional
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

        NotificationMessage notification = notificationHistoryCommandPort.saveNotification(
                NotificationMessage.requested(
                        command.userId(),
                        command.title(),
                        command.body(),
                        command.linkUrl(),
                        command.notificationId()
                )
        );

        notificationHistoryCommandPort.saveOutbox(NotificationOutbox.pendingWebPush(
                notification.getId(),
                command.userId(),
                command.title(),
                command.body(),
                command.linkUrl(),
                command.notificationId(),
                writeDataJson(command.data())
        ));

        return WebPushSendResult.builder()
                .total(0)
                .success(0)
                .failure(0)
                .expired(0)
                .skippedByDeduplication(false)
                .build();
    }

    public void processPendingOutboxes(int limit) {
        List<NotificationOutbox> outboxes = notificationHistoryCommandPort.claimPendingOutboxes(limit);
        for (NotificationOutbox outbox : outboxes) {
            processOutbox(outbox);
        }
    }

    private void processOutbox(NotificationOutbox outbox) {
        List<NotificationTargetProjection> targets = notificationTargetQueryPort.findActiveTargetsByUserId(outbox.getUserId());
        WebPushPayload payload = new WebPushPayload(
                outbox.getTitle(),
                outbox.getBody(),
                outbox.getLinkUrl(),
                outbox.getDeduplicationId(),
                mergeLinkUrl(readDataJson(outbox.getDataJson()), outbox.getLinkUrl())
        );

        int success = 0;
        int failure = 0;
        int expired = 0;

        for (NotificationTargetProjection target : targets) {
            WebPushSenderPort.WebPushSendResponse response = webPushSenderPort.send(target, payload);
            if (response.success()) {
                notificationTargetCommandPort.markUsed(target.id(), LocalDateTime.now());
                notificationHistoryCommandPort.saveDelivery(NotificationDelivery.success(
                        outbox.getNotificationId(),
                        outbox.getId(),
                        target.id(),
                        target.userId(),
                        target.provider(),
                        LocalDateTime.now()
                ));
                success++;
                continue;
            }

            failure++;
            if (response.expired()) {
                notificationTargetCommandPort.expireByEndpoint(target.endpoint());
                expired++;
            }

            notificationHistoryCommandPort.saveDelivery(NotificationDelivery.failed(
                    outbox.getNotificationId(),
                    outbox.getId(),
                    target.id(),
                    target.userId(),
                    target.provider(),
                    response.expired() ? NotificationDeliveryStatus.EXPIRED : NotificationDeliveryStatus.FAILED,
                    response.expired() ? "subscription expired" : "web push send failed",
                    LocalDateTime.now()
            ));
        }

        if (failure == 0) {
            notificationHistoryCommandPort.markOutboxSent(outbox.getId());
            return;
        }

        if (success > 0 || expired > 0) {
            notificationHistoryCommandPort.markOutboxSent(outbox.getId());
            return;
        }

        notificationHistoryCommandPort.markOutboxFailed(outbox.getId(), "all web push deliveries failed");
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

    private String writeDataJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data == null ? Map.of() : data);
        } catch (JacksonException ex) {
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readDataJson(String dataJson) {
        if (dataJson == null || dataJson.isBlank()) {
            return Map.of();
        }

        try {
            return objectMapper.readValue(dataJson, Map.class);
        } catch (JacksonException ex) {
            return Map.of();
        }
    }
}
