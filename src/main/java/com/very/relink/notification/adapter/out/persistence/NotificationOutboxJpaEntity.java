package com.very.relink.notification.adapter.out.persistence;

import com.very.relink.core.domain.BaseEntity;
import com.very.relink.notification.domain.model.NotificationChannel;
import com.very.relink.notification.domain.model.NotificationOutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "notification_outbox",
        indexes = {
                @Index(name = "idx_notification_outbox_status_created", columnList = "status,created_at")
        }
)
public class NotificationOutboxJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_outbox_id")
    private Long id;

    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private NotificationChannel channel;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "body", nullable = false, length = 2048)
    private String body;

    @Column(name = "link_url", length = 2048)
    private String linkUrl;

    @Column(name = "deduplication_id")
    private Long deduplicationId;

    @Column(name = "data_json", columnDefinition = "TEXT")
    private String dataJson;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationOutboxStatus status;

    @Setter
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Setter
    @Column(name = "failure_reason", length = 1024)
    private String failureReason;
}
