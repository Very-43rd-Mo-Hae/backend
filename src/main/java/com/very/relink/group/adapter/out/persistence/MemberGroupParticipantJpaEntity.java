package com.very.relink.group.adapter.out.persistence;

import com.very.relink.core.domain.BaseEntity;
import com.very.relink.group.domain.MemberGroupParticipantRole;
import com.very.relink.group.domain.MemberGroupParticipantStatus;
import com.very.relink.member.adapter.out.persistence.MemberJpaEntity;
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
        name = "member_group_participant",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_member_group_participant_group_member",
                        columnNames = {"member_group_id", "member_id"}
                )
        },
        indexes = {
                @Index(name = "idx_member_group_participant_member_status", columnList = "member_id,status")
        }
)
public class MemberGroupParticipantJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_group_participant_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_group_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_member_group_participant_group")
    )
    private MemberGroupJpaEntity memberGroup;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_member_group_participant_member")
    )
    private MemberJpaEntity member;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private MemberGroupParticipantRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private MemberGroupParticipantStatus status;
}
