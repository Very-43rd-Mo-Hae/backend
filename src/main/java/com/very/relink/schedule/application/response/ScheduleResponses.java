package com.very.relink.schedule.application.response;

import com.very.relink.schedule.domain.ScheduleSlotStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class ScheduleResponses {

    private ScheduleResponses() {
    }

    public record WeeklyScheduleResponse(
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            List<DailyScheduleResponse> days
    ) {
    }

    public record UpcomingScheduleStatusResponse(
            LocalDateTime from,
            LocalDateTime to,
            List<MemberScheduleStatusResponse> members
    ) {
    }

    public record MemberScheduleStatusResponse(
            Long memberId,
            List<UpcomingScheduleSlotResponse> slots
    ) {
    }

    public record UpcomingScheduleSlotResponse(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            ScheduleSlotStatus status,
            Long appointmentId
    ) {
    }

    public record DailyScheduleResponse(
            LocalDate date,
            List<ScheduleSlotResponse> slots
    ) {
    }

    public record ScheduleSlotResponse(
            LocalTime startTime,
            LocalTime endTime,
            ScheduleSlotStatus status,
            Long appointmentId
    ) {
    }

    public record UpdateScheduleSlotsResponse(
            LocalDate weekStartDate,
            List<ScheduleSlotChangeResponse> slots
    ) {
    }

    public record ScheduleSlotChangeResponse(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            ScheduleSlotStatus status
    ) {
    }
}
