package com.very.relink.appointment.adapter.out.persistence;

import com.very.relink.core.domain.BaseEntity;
import com.very.relink.member.adapter.out.persistence.MemberJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
        name = "appointment_reminder",
        indexes = {
                @Index(name = "idx_appointment_reminder_due", columnList = "sent_at,remind_at")
        }
)
public class AppointmentReminderJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_reminder_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "appointment_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_appointment_reminder_appointment")
    )
    private AppointmentJpaEntity appointment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_appointment_reminder_member")
    )
    private MemberJpaEntity member;

    @Column(name = "remind_at", nullable = false)
    private LocalDateTime remindAt;

    @Column(name = "minutes_before", nullable = false)
    private int minutesBefore;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public static AppointmentReminderJpaEntity create(
            AppointmentJpaEntity appointment,
            MemberJpaEntity member,
            LocalDateTime remindAt,
            int minutesBefore
    ) {
        return AppointmentReminderJpaEntity.builder()
                .appointment(appointment)
                .member(member)
                .remindAt(remindAt)
                .minutesBefore(minutesBefore)
                .build();
    }

    public void markSent(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
