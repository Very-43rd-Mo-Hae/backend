package com.very.relink.appointment.adapter.out.persistence;

import com.very.relink.core.domain.BaseEntity;
import com.very.relink.group.adapter.out.persistence.MemberGroupJpaEntity;
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
        name = "appointment",
        indexes = {
                @Index(name = "idx_appointment_owner_start", columnList = "owner_member_id,start_at"),
                @Index(name = "idx_appointment_group_start", columnList = "member_group_id,start_at")
        }
)
public class AppointmentJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "owner_member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_appointment_owner")
    )
    private MemberJpaEntity owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "member_group_id",
            foreignKey = @ForeignKey(name = "fk_appointment_member_group")
    )
    private MemberGroupJpaEntity memberGroup;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "memo", length = 1000)
    private String memo;
}
