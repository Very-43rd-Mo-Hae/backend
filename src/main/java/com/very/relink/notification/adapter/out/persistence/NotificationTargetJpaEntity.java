package com.very.relink.notification.adapter.out.persistence;

import com.very.relink.core.domain.BaseEntity;
import com.very.relink.notification.domain.model.NotificationChannel;
import com.very.relink.notification.domain.model.NotificationTargetStatus;
import com.very.relink.notification.domain.model.PushProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "notification_target",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notification_target_endpoint", columnNames = "endpoint")
        },
        indexes = {
                @Index(name = "idx_notification_target_user_status", columnList = "user_id,status"),
                @Index(name = "idx_notification_target_user_channel_status", columnList = "user_id,channel,status"),
                @Index(name = "idx_notification_target_channel_status", columnList = "channel,status")
        }
)
public class NotificationTargetJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_target_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private PushProvider provider;

    @Column(name = "endpoint", nullable = false, length = 2048)
    private String endpoint;

    @Column(name = "p256dh", nullable = false, length = 512)
    private String p256dh;

    @Column(name = "auth", nullable = false, length = 256)
    private String auth;

    @Column(name = "user_agent", length = 1024)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationTargetStatus status;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}
