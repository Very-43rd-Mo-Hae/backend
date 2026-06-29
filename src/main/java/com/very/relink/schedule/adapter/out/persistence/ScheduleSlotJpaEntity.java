package com.very.relink.schedule.adapter.out.persistence;

import com.very.relink.appointment.adapter.out.persistence.AppointmentJpaEntity;
import com.very.relink.core.domain.BaseEntity;
import com.very.relink.schedule.domain.ScheduleSlotStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalTime;
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
        name = "schedule_slot",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_schedule_slot_week_date_time",
                        columnNames = {"weekly_schedule_id", "schedule_date", "start_time", "end_time"}
                )
        },
        indexes = {
                @Index(name = "idx_schedule_slot_week_status", columnList = "weekly_schedule_id,status"),
                @Index(name = "idx_schedule_slot_appointment", columnList = "appointment_id")
        }
)
public class ScheduleSlotJpaEntity extends BaseEntity {

    private static final int SLOT_MINUTES = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_slot_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "weekly_schedule_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_schedule_slot_weekly_schedule")
    )
    private WeeklyScheduleJpaEntity weeklySchedule;

    @Column(name = "schedule_date", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ScheduleSlotStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "appointment_id",
            foreignKey = @ForeignKey(name = "fk_schedule_slot_appointment")
    )
    private AppointmentJpaEntity appointment;

    public static ScheduleSlotJpaEntity create(
            WeeklyScheduleJpaEntity weeklySchedule,
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime,
            ScheduleSlotStatus status
    ) {
        return ScheduleSlotJpaEntity.builder()
                .weeklySchedule(weeklySchedule)
                .scheduleDate(scheduleDate)
                .startTime(startTime)
                .endTime(endTime)
                .status(status)
                .build();
    }

    public void updateStatus(ScheduleSlotStatus status) {
        this.status = status;
    }

    @PrePersist
    @PreUpdate
    private void validateThirtyMinuteSlot() {
        if (startTime == null || endTime == null) {
            return;
        }
        if (startTime.getMinute() % SLOT_MINUTES != 0 || startTime.getSecond() != 0 || startTime.getNano() != 0) {
            throw new IllegalArgumentException("Schedule slot start time must be aligned to 30 minutes.");
        }
        if (!endTime.equals(startTime.plusMinutes(SLOT_MINUTES))) {
            throw new IllegalArgumentException("Schedule slot must be exactly 30 minutes.");
        }
    }
}
