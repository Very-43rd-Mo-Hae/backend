package com.very.relink.friend.adapter.out.persistence;

import com.very.relink.core.domain.BaseEntity;
import com.very.relink.friend.domain.FriendshipStatus;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
        name = "friendship",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_friendship_member_pair",
                        columnNames = {"member_low_id", "member_high_id"}
                )
        },
        indexes = {
                @Index(name = "idx_friendship_low_status", columnList = "member_low_id,status"),
                @Index(name = "idx_friendship_high_status", columnList = "member_high_id,status"),
                @Index(name = "idx_friendship_requester_status", columnList = "requester_member_id,status")
        }
)
public class FriendshipJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friendship_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "requester_member_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_friendship_requester_member")
    )
    private MemberJpaEntity requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_low_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_friendship_low_member")
    )
    private MemberJpaEntity memberLow;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "member_high_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_friendship_high_member")
    )
    private MemberJpaEntity memberHigh;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private FriendshipStatus status;

    @PrePersist
    @PreUpdate
    private void validateMemberPair() {
        if (memberLow == null || memberHigh == null || memberLow.getId() == null || memberHigh.getId() == null) {
            return;
        }
        if (memberLow.getId().equals(memberHigh.getId())) {
            throw new IllegalArgumentException("Friendship members must be different.");
        }
        if (memberLow.getId() > memberHigh.getId()) {
            throw new IllegalArgumentException("Friendship member pair must be ordered by member id.");
        }
    }
}
