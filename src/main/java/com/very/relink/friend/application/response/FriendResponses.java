package com.very.relink.friend.application.response;

import com.very.relink.schedule.domain.ScheduleSlotStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class FriendResponses {

    private FriendResponses() {
    }

    public record FriendListResponse(
            long totalCount,
            int page,
            int size,
            boolean hasNext,
            List<FriendSummaryResponse> friends
    ) {
    }

    public record RecommendedFriendListResponse(
            List<FriendSummaryResponse> friends
    ) {
    }

    public record FriendSummaryResponse(
            Long memberId,
            String name,
            String imageUrl
    ) {
    }

    public record FriendStatusListResponse(
            LocalDateTime from,
            LocalDateTime to,
            List<FriendStatusResponse> friends
    ) {
    }

    public record FriendStatusResponse(
            Long memberId,
            boolean active,
            List<FriendStatusSlotResponse> slots
    ) {
    }

    public record FriendStatusSlotResponse(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            ScheduleSlotStatus status,
            Long appointmentId
    ) {
    }
}
