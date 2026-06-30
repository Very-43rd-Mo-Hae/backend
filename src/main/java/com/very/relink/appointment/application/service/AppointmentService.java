package com.very.relink.appointment.application.service;

import com.very.relink.appointment.adapter.out.persistence.AppointmentJpaEntity;
import com.very.relink.appointment.adapter.out.persistence.AppointmentJpaRepository;
import com.very.relink.appointment.adapter.out.persistence.AppointmentParticipantJpaEntity;
import com.very.relink.appointment.adapter.out.persistence.AppointmentParticipantJpaRepository;
import com.very.relink.appointment.adapter.out.persistence.AppointmentReminderJpaEntity;
import com.very.relink.appointment.adapter.out.persistence.AppointmentReminderJpaRepository;
import com.very.relink.appointment.application.response.AppointmentResponses.AppointmentParticipantResponse;
import com.very.relink.appointment.application.response.AppointmentResponses.AppointmentResponse;
import com.very.relink.appointment.application.response.AppointmentResponses.AvailableFriendListResponse;
import com.very.relink.appointment.application.response.AppointmentResponses.AvailableFriendResponse;
import com.very.relink.appointment.domain.AppointmentParticipantStatus;
import com.very.relink.appointment.exception.AppointmentErrorCode;
import com.very.relink.friend.adapter.out.persistence.FriendshipJpaRepository;
import com.very.relink.member.adapter.out.persistence.MemberJpaEntity;
import com.very.relink.member.adapter.out.persistence.MemberJpaRepository;
import com.very.relink.notification.application.command.SendWebPushNotificationCommand;
import com.very.relink.notification.application.service.WebPushNotificationService;
import com.very.relink.schedule.adapter.out.persistence.ScheduleSlotJpaEntity;
import com.very.relink.schedule.adapter.out.persistence.ScheduleSlotJpaRepository;
import com.very.relink.schedule.adapter.out.persistence.WeeklyScheduleJpaEntity;
import com.very.relink.schedule.adapter.out.persistence.WeeklyScheduleJpaRepository;
import com.very.relink.schedule.application.service.ScheduleService;
import com.very.relink.schedule.domain.ScheduleSlotStatus;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final int SLOT_MINUTES = 30;
    private static final List<Integer> REMINDER_MINUTES = List.of(24 * 60, 3 * 60, 60);

    private final MemberJpaRepository memberJpaRepository;
    private final FriendshipJpaRepository friendshipJpaRepository;
    private final AppointmentJpaRepository appointmentJpaRepository;
    private final AppointmentParticipantJpaRepository appointmentParticipantJpaRepository;
    private final AppointmentReminderJpaRepository appointmentReminderJpaRepository;
    private final WeeklyScheduleJpaRepository weeklyScheduleJpaRepository;
    private final ScheduleSlotJpaRepository scheduleSlotJpaRepository;
    private final ScheduleService scheduleService;
    private final WebPushNotificationService webPushNotificationService;

    @Transactional(readOnly = true)
    public AvailableFriendListResponse getAvailableFriends(Long ownerId, LocalDateTime startAt, LocalDateTime endAt) {
        validateAppointmentTime(startAt, endAt);
        validateMemberExists(ownerId);

        LocalDate scheduleDate = startAt.toLocalDate();
        LocalTime startTime = startAt.toLocalTime();
        LocalTime endTime = endAt.toLocalTime();
        List<MemberJpaEntity> friends = friendshipJpaRepository.findAcceptedFriendMembers(ownerId);
        List<Long> friendIds = friends.stream().map(MemberJpaEntity::getId).toList();
        Map<Long, List<ScheduleSlotJpaEntity>> slotMap = toMemberSlots(
                friendIds.isEmpty()
                        ? List.of()
                        : scheduleSlotJpaRepository.findAllByMemberIdsAndDateTimeRange(
                                friendIds,
                                scheduleDate,
                                startTime,
                                endTime
                        )
        );

        List<AvailableFriendResponse> availableFriends = friends.stream()
                .filter(friend -> isAvailable(friend.getId(), slotMap.getOrDefault(friend.getId(), List.of())))
                .map(friend -> new AvailableFriendResponse(
                        friend.getId(),
                        friend.getName(),
                        friend.getImageUrl(),
                        scheduleService.getWeeklySchedule(friend.getId(), scheduleDate)
                ))
                .toList();

        return new AvailableFriendListResponse(startAt, endAt, availableFriends);
    }

    @Transactional
    public AppointmentResponse createAppointment(
            Long ownerId,
            String title,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String memo,
            List<Long> participantMemberIds
    ) {
        validateAppointmentTime(startAt, endAt);
        MemberJpaEntity owner = memberJpaRepository.findById(ownerId)
                .orElseThrow(AppointmentErrorCode.MEMBER_NOT_FOUND::toException);
        List<MemberJpaEntity> participants = loadParticipants(owner, participantMemberIds);
        validateParticipantsAvailable(participants, startAt, endAt);

        AppointmentJpaEntity appointment = appointmentJpaRepository.save(AppointmentJpaEntity.builder()
                .owner(owner)
                .title(normalizeTitle(title))
                .startAt(startAt)
                .endAt(endAt)
                .memo(memo)
                .build());

        for (MemberJpaEntity participant : participants) {
            appointmentParticipantJpaRepository.save(AppointmentParticipantJpaEntity.builder()
                    .appointment(appointment)
                    .member(participant)
                    .status(AppointmentParticipantStatus.ACCEPTED)
                    .build());
        }

        blockParticipantSchedules(appointment, participants);
        sendCreatedNotifications(appointment, participants);
        scheduleReminderNotifications(appointment, participants);

        return toAppointmentResponse(appointment, participants);
    }

    private List<MemberJpaEntity> loadParticipants(MemberJpaEntity owner, List<Long> participantMemberIds) {
        if (participantMemberIds == null || participantMemberIds.isEmpty()) {
            throw AppointmentErrorCode.INVALID_PARTICIPANTS.toException();
        }

        Set<Long> ids = new LinkedHashSet<>();
        ids.add(owner.getId());
        for (Long memberId : participantMemberIds) {
            if (memberId == null) {
                throw AppointmentErrorCode.INVALID_PARTICIPANTS.toException();
            }
            if (!memberId.equals(owner.getId())
                    && !friendshipJpaRepository.existsAcceptedFriendship(owner.getId(), memberId)) {
                throw AppointmentErrorCode.FRIEND_NOT_FOUND.toException();
            }
            ids.add(memberId);
        }

        List<MemberJpaEntity> members = memberJpaRepository.findAllById(ids);
        if (members.size() != ids.size()) {
            throw AppointmentErrorCode.MEMBER_NOT_FOUND.toException();
        }

        Map<Long, MemberJpaEntity> byId = new LinkedHashMap<>();
        members.forEach(member -> byId.put(member.getId(), member));
        return ids.stream()
                .map(byId::get)
                .toList();
    }

    private void validateParticipantsAvailable(
            List<MemberJpaEntity> participants,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        List<Long> memberIds = participants.stream().map(MemberJpaEntity::getId).toList();
        Map<Long, List<ScheduleSlotJpaEntity>> slotMap = toMemberSlots(
                scheduleSlotJpaRepository.findAllByMemberIdsAndDateTimeRange(
                        memberIds,
                        startAt.toLocalDate(),
                        startAt.toLocalTime(),
                        endAt.toLocalTime()
                )
        );

        for (MemberJpaEntity participant : participants) {
            if (!isAvailable(participant.getId(), slotMap.getOrDefault(participant.getId(), List.of()))) {
                throw AppointmentErrorCode.PARTICIPANT_UNAVAILABLE.toException();
            }
        }
    }

    private boolean isAvailable(Long memberId, List<ScheduleSlotJpaEntity> savedSlots) {
        return savedSlots.stream()
                .filter(slot -> slot.getWeeklySchedule().getMember().getId().equals(memberId))
                .noneMatch(slot -> slot.getStatus() == ScheduleSlotStatus.UNAVAILABLE
                        || slot.getStatus() == ScheduleSlotStatus.APPOINTMENT);
    }

    private void blockParticipantSchedules(AppointmentJpaEntity appointment, List<MemberJpaEntity> participants) {
        LocalDate date = appointment.getStartAt().toLocalDate();
        LocalTime startTime = appointment.getStartAt().toLocalTime();
        LocalTime endTime = appointment.getEndAt().toLocalTime();

        for (MemberJpaEntity participant : participants) {
            WeeklyScheduleJpaEntity weeklySchedule = getOrCreateWeeklySchedule(participant, date);
            for (LocalTime slotStart = startTime; slotStart.isBefore(endTime); slotStart = slotStart.plusMinutes(SLOT_MINUTES)) {
                LocalTime slotEnd = slotStart.plusMinutes(SLOT_MINUTES);
                LocalTime currentSlotStart = slotStart;
                LocalTime currentSlotEnd = slotEnd;
                scheduleSlotJpaRepository
                        .findByWeeklySchedule_IdAndScheduleDateAndStartTimeAndEndTime(
                                weeklySchedule.getId(),
                                date,
                                currentSlotStart,
                                currentSlotEnd
                        )
                        .ifPresentOrElse(
                                slot -> slot.assignAppointment(appointment),
                                () -> scheduleSlotJpaRepository.save(ScheduleSlotJpaEntity.builder()
                                        .weeklySchedule(weeklySchedule)
                                        .scheduleDate(date)
                                        .startTime(currentSlotStart)
                                        .endTime(currentSlotEnd)
                                        .status(ScheduleSlotStatus.APPOINTMENT)
                                        .appointment(appointment)
                                        .build())
                        );
            }
        }
    }

    private WeeklyScheduleJpaEntity getOrCreateWeeklySchedule(MemberJpaEntity member, LocalDate date) {
        LocalDate weekStartDate = date.minusDays(date.getDayOfWeek().getValue() - 1L);
        return weeklyScheduleJpaRepository.findByMember_IdAndWeekStartDate(member.getId(), weekStartDate)
                .orElseGet(() -> weeklyScheduleJpaRepository.save(WeeklyScheduleJpaEntity.create(member, weekStartDate)));
    }

    private void sendCreatedNotifications(AppointmentJpaEntity appointment, List<MemberJpaEntity> participants) {
        for (MemberJpaEntity participant : participants) {
            webPushNotificationService.send(new SendWebPushNotificationCommand(
                    participant.getId(),
                    "Appointment created",
                    appointment.getTitle() + " has been added to your calendar.",
                    "/calendar",
                    null,
                    Map.of("type", "APPOINTMENT_CREATED", "appointmentId", appointment.getId())
            ));
        }
    }

    private void scheduleReminderNotifications(AppointmentJpaEntity appointment, List<MemberJpaEntity> participants) {
        LocalDateTime now = LocalDateTime.now();
        for (MemberJpaEntity participant : participants) {
            for (Integer minutesBefore : REMINDER_MINUTES) {
                LocalDateTime remindAt = appointment.getStartAt().minusMinutes(minutesBefore);
                if (remindAt.isAfter(now)) {
                    appointmentReminderJpaRepository.save(AppointmentReminderJpaEntity.create(
                            appointment,
                            participant,
                            remindAt,
                            minutesBefore
                    ));
                }
            }
        }
    }

    private Map<Long, List<ScheduleSlotJpaEntity>> toMemberSlots(List<ScheduleSlotJpaEntity> slots) {
        Map<Long, List<ScheduleSlotJpaEntity>> slotMap = new LinkedHashMap<>();
        for (ScheduleSlotJpaEntity slot : slots) {
            Long memberId = slot.getWeeklySchedule().getMember().getId();
            slotMap.computeIfAbsent(memberId, ignored -> new ArrayList<>()).add(slot);
        }
        return slotMap;
    }

    private AppointmentResponse toAppointmentResponse(
            AppointmentJpaEntity appointment,
            List<MemberJpaEntity> participants
    ) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getTitle(),
                appointment.getStartAt(),
                appointment.getEndAt(),
                appointment.getMemo(),
                participants.stream()
                        .sorted(Comparator.comparing(MemberJpaEntity::getId))
                        .map(member -> new AppointmentParticipantResponse(
                                member.getId(),
                                member.getName(),
                                member.getImageUrl()
                        ))
                        .toList()
        );
    }

    private String normalizeTitle(String title) {
        if (title == null || title.isBlank()) {
            return "Appointment";
        }
        return title.trim();
    }

    private void validateMemberExists(Long memberId) {
        if (!memberJpaRepository.existsById(memberId)) {
            throw AppointmentErrorCode.MEMBER_NOT_FOUND.toException();
        }
    }

    private void validateAppointmentTime(LocalDateTime startAt, LocalDateTime endAt) {
        if (startAt == null || endAt == null
                || !startAt.toLocalDate().equals(endAt.toLocalDate())
                || !startAt.isBefore(endAt)
                || Duration.between(startAt, endAt).toMinutes() % SLOT_MINUTES != 0
                || !isAligned(startAt)
                || !isAligned(endAt)) {
            throw AppointmentErrorCode.INVALID_APPOINTMENT_TIME.toException();
        }
    }

    private boolean isAligned(LocalDateTime dateTime) {
        return dateTime.getMinute() % SLOT_MINUTES == 0
                && dateTime.getSecond() == 0
                && dateTime.getNano() == 0;
    }
}
