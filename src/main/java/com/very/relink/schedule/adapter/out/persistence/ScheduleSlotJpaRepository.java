package com.very.relink.schedule.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScheduleSlotJpaRepository extends JpaRepository<ScheduleSlotJpaEntity, Long> {

    List<ScheduleSlotJpaEntity> findByWeeklySchedule_IdOrderByScheduleDateAscStartTimeAsc(Long weeklyScheduleId);

    @Query("""
            select s
            from ScheduleSlotJpaEntity s
            join fetch s.weeklySchedule w
            join fetch w.member m
            left join fetch s.appointment
            where m.id in :memberIds
              and s.scheduleDate between :startDate and :endDate
            order by m.id asc, s.scheduleDate asc, s.startTime asc
            """)
    List<ScheduleSlotJpaEntity> findAllByMemberIdsAndScheduleDateBetween(
            @Param("memberIds") List<Long> memberIds,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    Optional<ScheduleSlotJpaEntity> findByWeeklySchedule_IdAndScheduleDateAndStartTimeAndEndTime(
            Long weeklyScheduleId,
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime
    );

    @Query("""
            select s
            from ScheduleSlotJpaEntity s
            join fetch s.weeklySchedule w
            join fetch w.member m
            left join fetch s.appointment
            where m.id in :memberIds
              and s.scheduleDate = :scheduleDate
              and s.startTime >= :startTime
              and s.endTime <= :endTime
            order by m.id asc, s.startTime asc
            """)
    List<ScheduleSlotJpaEntity> findAllByMemberIdsAndDateTimeRange(
            @Param("memberIds") List<Long> memberIds,
            @Param("scheduleDate") LocalDate scheduleDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}
