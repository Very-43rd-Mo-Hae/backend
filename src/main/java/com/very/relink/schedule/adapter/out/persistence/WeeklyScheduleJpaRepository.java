package com.very.relink.schedule.adapter.out.persistence;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeeklyScheduleJpaRepository extends JpaRepository<WeeklyScheduleJpaEntity, Long> {

    Optional<WeeklyScheduleJpaEntity> findByMember_IdAndWeekStartDate(Long memberId, LocalDate weekStartDate);
}
