package com.very.relink.schedule.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleSlotJpaRepository extends JpaRepository<ScheduleSlotJpaEntity, Long> {

    List<ScheduleSlotJpaEntity> findByWeeklySchedule_IdOrderByScheduleDateAscStartTimeAsc(Long weeklyScheduleId);

    Optional<ScheduleSlotJpaEntity> findByWeeklySchedule_IdAndScheduleDateAndStartTimeAndEndTime(
            Long weeklyScheduleId,
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime
    );
}
