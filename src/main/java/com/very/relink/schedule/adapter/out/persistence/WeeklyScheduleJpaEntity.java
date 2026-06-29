package com.very.relink.schedule.adapter.out.persistence;

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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
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
        name = "weekly_schedule",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_weekly_schedule_member_week",
                        columnNames = {"member_id", "week_start_date"}
                )
        },
        indexes = {
                @Index(name = "idx_weekly_schedule_member_week", columnList = "member_id,week_start_date")
        }
)
public class WeeklyScheduleJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weekly_schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_weekly_schedule_member")
    )
    private MemberJpaEntity member;

    @Column(name = "week_start_date", nullable = false)
    private LocalDate weekStartDate;

    public static WeeklyScheduleJpaEntity create(MemberJpaEntity member, LocalDate weekStartDate) {
        return WeeklyScheduleJpaEntity.builder()
                .member(member)
                .weekStartDate(weekStartDate)
                .build();
    }
}
