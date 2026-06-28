package com.very.relink.notification.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationTarget {

    private final Long id;
    private Long userId;
    private NotificationChannel channel;
    private PushProvider provider;
    private String endpoint;
    private String p256dh;
    private String auth;
    private String userAgent;
    private NotificationTargetStatus status;
    private LocalDateTime lastUsedAt;

    public static NotificationTarget createWebPush(
            Long userId,
            String endpoint,
            String p256dh,
            String auth,
            String userAgent,
            PushProvider provider
    ) {
        return NotificationTarget.builder()
                .userId(userId)
                .channel(NotificationChannel.WEB_PUSH)
                .provider(provider)
                .endpoint(endpoint)
                .p256dh(p256dh)
                .auth(auth)
                .userAgent(userAgent)
                .status(NotificationTargetStatus.ACTIVE)
                .build();
    }

    public void activate() {
        this.status = NotificationTargetStatus.ACTIVE;
    }

    public void disable() {
        this.status = NotificationTargetStatus.DISABLED;
    }

    public void expire() {
        this.status = NotificationTargetStatus.EXPIRED;
    }

    public void refreshSubscription(Long userId, String p256dh, String auth, String userAgent, PushProvider provider) {
        this.userId = userId;
        this.p256dh = p256dh;
        this.auth = auth;
        this.userAgent = userAgent;
        this.provider = provider;
        activate();
    }

    public void markUsed(LocalDateTime usedAt) {
        this.lastUsedAt = usedAt;
    }

    public boolean isActive() {
        return status == NotificationTargetStatus.ACTIVE;
    }
}
