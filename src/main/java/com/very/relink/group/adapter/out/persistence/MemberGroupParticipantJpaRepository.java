package com.very.relink.group.adapter.out.persistence;

import com.very.relink.group.domain.MemberGroupParticipantStatus;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberGroupParticipantJpaRepository extends JpaRepository<MemberGroupParticipantJpaEntity, Long> {

    @Query("""
            select
                g.id as groupId,
                g.name as name,
                count(activeParticipant.id) as memberCount
            from MemberGroupParticipantJpaEntity participant
            join participant.memberGroup g
            join MemberGroupParticipantJpaEntity activeParticipant
                on activeParticipant.memberGroup = g
               and activeParticipant.status = :activeStatus
            where participant.member.id = :memberId
              and participant.status = :activeStatus
            group by g.id, g.name
            order by g.name asc, g.id asc
            """)
    List<MemberGroupSummaryProjection> findActiveGroupSummariesByMemberId(
            @Param("memberId") Long memberId,
            @Param("activeStatus") MemberGroupParticipantStatus activeStatus,
            Pageable pageable
    );
}
