package com.very.relink.notification.adapter.out.persistence;

import com.very.relink.core.domain.BaseEntity;
import com.very.relink.notification.domain.model.NotificationChannel;
import com.very.relink.notification.domain.model.NotificationDeliveryStatus;
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
        name = "notification_delivery",
        indexes = {
                @Index(name = "idx_notification_delivery_notification", columnList = "notification_id"),
                @Index(name = "idx_notification_delivery_user_status", columnList = "user_id,status")
        }
)
public class NotificationDeliveryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_delivery_id")
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(name = "notification_outbox_id", nullable = false)
    private Long outboxId;

    @Column(name = "notification_target_id", nullable = false)
    private Long targetId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private PushProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationDeliveryStatus status;

    @Column(name = "failure_reason", length = 1024)
    private String failureReason;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
