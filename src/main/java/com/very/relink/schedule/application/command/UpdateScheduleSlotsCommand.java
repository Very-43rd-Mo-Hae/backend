package com.very.relink.schedule.application.command;

import com.very.relink.schedule.domain.ScheduleSlotStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record UpdateScheduleSlotsCommand(
        Long memberId,
        List<ScheduleSlotUpdateCommand> slots
) {

    public record ScheduleSlotUpdateCommand(
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime,
            ScheduleSlotStatus status
    ) {
    }
}
