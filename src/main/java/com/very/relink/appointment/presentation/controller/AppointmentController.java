package com.very.relink.appointment.presentation.controller;

import com.very.relink.appointment.application.response.AppointmentResponses.AppointmentResponse;
import com.very.relink.appointment.application.response.AppointmentResponses.AvailableFriendListResponse;
import com.very.relink.appointment.application.response.AppointmentResponses.FriendCalendarListResponse;
import com.very.relink.appointment.application.service.AppointmentService;
import com.very.relink.core.presentation.RestResponse;
import com.very.relink.notification.application.port.in.CurrentUserProvider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final CurrentUserProvider currentUserProvider;
    private final AppointmentService appointmentService;

    @GetMapping("/available-friends")
    public ResponseEntity<RestResponse<AvailableFriendListResponse>> getAvailableFriends(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt
    ) {
        Long memberId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(
                appointmentService.getAvailableFriends(memberId, startAt, endAt)
        ));
    }

    @GetMapping("/friend-calendars")
    public ResponseEntity<RestResponse<FriendCalendarListResponse>> getFriendCalendars(
            @RequestParam List<Long> memberIds,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate date
    ) {
        Long memberId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(
                appointmentService.getFriendCalendars(memberId, memberIds, date)
        ));
    }

    @PostMapping
    public ResponseEntity<RestResponse<AppointmentResponse>> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request
    ) {
        Long memberId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(
                appointmentService.createAppointment(
                        memberId,
                        request.title(),
                        request.startAt(),
                        request.endAt(),
                        request.memo(),
                        request.participantMemberIds()
                )
        ));
    }

    public record CreateAppointmentRequest(
            @Size(max = 100) String title,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt,
            @Size(max = 1000) String memo,
            @NotEmpty List<@NotNull Long> participantMemberIds
    ) {
    }
}
