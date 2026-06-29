package com.very.relink.schedule.presentation.controller;

import com.very.relink.core.presentation.RestResponse;
import com.very.relink.notification.application.port.in.CurrentUserProvider;
import com.very.relink.schedule.application.command.UpdateScheduleSlotsCommand;
import com.very.relink.schedule.application.command.UpdateScheduleSlotsCommand.ScheduleSlotUpdateCommand;
import com.very.relink.schedule.application.response.ScheduleResponses.UpdateScheduleSlotsResponse;
import com.very.relink.schedule.application.response.ScheduleResponses.WeeklyScheduleResponse;
import com.very.relink.schedule.application.service.ScheduleService;
import com.very.relink.schedule.domain.ScheduleSlotStatus;
import com.very.relink.schedule.presentation.swagger.ScheduleSwagger;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schedules")
public class ScheduleController implements ScheduleSwagger {

    private final CurrentUserProvider currentUserProvider;
    private final ScheduleService scheduleService;

    @GetMapping("/week")
    @Override
    public ResponseEntity<RestResponse<WeeklyScheduleResponse>> getWeeklySchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        Long memberId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(
                scheduleService.getWeeklySchedule(memberId, date)
        ));
    }

    @PatchMapping("/slots")
    @Override
    public ResponseEntity<RestResponse<UpdateScheduleSlotsResponse>> updateSlots(
            @Valid @RequestBody UpdateScheduleSlotsRequest request
    ) {
        Long memberId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(
                scheduleService.updateSlots(new UpdateScheduleSlotsCommand(
                        memberId,
                        request.slots().stream()
                                .map(slot -> new ScheduleSlotUpdateCommand(
                                        slot.scheduleDate(),
                                        slot.startTime(),
                                        slot.endTime(),
                                        slot.status()
                                ))
                                .toList()
                ))
        ));
    }

    public record UpdateScheduleSlotsRequest(
            @NotEmpty List<@Valid ScheduleSlotUpdateRequest> slots
    ) {
    }

    public record ScheduleSlotUpdateRequest(
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate scheduleDate,
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime,
            @NotNull ScheduleSlotStatus status
    ) {
    }
}
