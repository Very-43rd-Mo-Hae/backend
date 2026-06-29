package com.very.relink.notification.adapter.out.persistence;

import com.very.relink.core.domain.BaseEntity;
import com.very.relink.member.adapter.out.persistence.MemberJpaEntity;
import com.very.relink.notification.domain.model.NotificationInboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
        name = "notification_inbox_item",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_inbox_member_notification",
                        columnNames = {"member_id", "notification_id"}
                )
        },
        indexes = {
                @Index(name = "idx_notification_inbox_member_status", columnList = "member_id,status"),
                @Index(name = "idx_notification_inbox_member_created", columnList = "member_id,created_at")
        }
)
public class NotificationInboxItemJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_inbox_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_notification_inbox_member")
    )
    private MemberJpaEntity member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "notification_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_notification_inbox_notification")
    )
    private NotificationJpaEntity notification;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private NotificationInboxStatus status;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}
