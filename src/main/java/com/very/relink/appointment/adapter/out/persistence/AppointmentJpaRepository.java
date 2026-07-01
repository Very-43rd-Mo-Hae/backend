package com.very.relink.appointment.adapter.out.persistence;

import com.very.relink.appointment.domain.AppointmentParticipantStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentJpaEntity, Long> {

    @Query("""
            select distinct appointment
            from AppointmentJpaEntity appointment
            left join AppointmentParticipantJpaEntity participant
                on participant.appointment = appointment
            where appointment.startAt >= :from
              and (
                appointment.owner.id = :memberId
                or (
                    participant.member.id = :memberId
                    and participant.status = :participantStatus
                )
              )
            order by appointment.startAt asc, appointment.id asc
            """)
    List<AppointmentJpaEntity> findUpcomingAppointments(
            @Param("memberId") Long memberId,
            @Param("participantStatus") AppointmentParticipantStatus participantStatus,
            @Param("from") LocalDateTime from,
            Pageable pageable
    );
}
