package com.very.relink.appointment.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentParticipantJpaRepository extends JpaRepository<AppointmentParticipantJpaEntity, Long> {

    @Query("""
            select participant
            from AppointmentParticipantJpaEntity participant
            join fetch participant.member
            where participant.appointment.id in :appointmentIds
            order by participant.appointment.id asc, participant.member.name asc, participant.member.id asc
            """)
    List<AppointmentParticipantJpaEntity> findAllByAppointmentIds(
            @Param("appointmentIds") List<Long> appointmentIds
    );
}
