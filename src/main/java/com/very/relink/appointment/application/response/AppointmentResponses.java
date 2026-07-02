package com.very.relink.appointment.application.response;

import com.very.relink.schedule.application.response.ScheduleResponses.WeeklyScheduleResponse;
import java.time.LocalDateTime;
import java.util.List;

public final class AppointmentResponses {

    private AppointmentResponses() {
    }

    public record AppointmentResponse(
            Long appointmentId,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String memo,
            List<AppointmentParticipantResponse> participants,
            Long chatRoomId,
            String inviteLink
    ) {
    }

    public record UpcomingAppointmentListResponse(
            List<AppointmentResponse> appointments
    ) {
    }

    public record AppointmentParticipantResponse(
            Long memberId,
            String name,
            String imageUrl
    ) {
    }

    public record AvailableFriendListResponse(
            LocalDateTime startAt,
            LocalDateTime endAt,
            List<AvailableFriendResponse> friends
    ) {
    }

    public record AvailableFriendResponse(
            Long memberId,
            String name,
            String imageUrl,
            WeeklyScheduleResponse calendar
    ) {
    }

    public record FriendCalendarListResponse(
            List<FriendCalendarResponse> friends
    ) {
    }

    public record FriendCalendarResponse(
            Long memberId,
            String name,
            String imageUrl,
            WeeklyScheduleResponse calendar
    ) {
    }
}
