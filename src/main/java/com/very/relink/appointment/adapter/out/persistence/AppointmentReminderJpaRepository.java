package com.very.relink.appointment.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentReminderJpaRepository extends JpaRepository<AppointmentReminderJpaEntity, Long> {

    @EntityGraph(attributePaths = {"appointment", "member"})
    List<AppointmentReminderJpaEntity> findBySentAtIsNullAndRemindAtLessThanEqualOrderByRemindAtAsc(
            LocalDateTime now,
            Pageable pageable
    );
}
