package com.very.relink.schedule.application.service;

import com.very.relink.member.adapter.out.persistence.MemberJpaEntity;
import com.very.relink.member.adapter.out.persistence.MemberJpaRepository;
import com.very.relink.schedule.adapter.out.persistence.ScheduleSlotJpaEntity;
import com.very.relink.schedule.adapter.out.persistence.ScheduleSlotJpaRepository;
import com.very.relink.schedule.adapter.out.persistence.WeeklyScheduleJpaEntity;
import com.very.relink.schedule.adapter.out.persistence.WeeklyScheduleJpaRepository;
import com.very.relink.schedule.application.command.UpdateScheduleSlotsCommand;
import com.very.relink.schedule.application.command.UpdateScheduleSlotsCommand.ScheduleSlotUpdateCommand;
import com.very.relink.schedule.application.response.ScheduleResponses.DailyScheduleResponse;
import com.very.relink.schedule.application.response.ScheduleResponses.MemberScheduleStatusResponse;
import com.very.relink.schedule.application.response.ScheduleResponses.ScheduleSlotChangeResponse;
import com.very.relink.schedule.application.response.ScheduleResponses.ScheduleSlotResponse;
import com.very.relink.schedule.application.response.ScheduleResponses.UpdateScheduleSlotsResponse;
import com.very.relink.schedule.application.response.ScheduleResponses.UpcomingScheduleSlotResponse;
import com.very.relink.schedule.application.response.ScheduleResponses.UpcomingScheduleStatusResponse;
import com.very.relink.schedule.application.response.ScheduleResponses.WeeklyScheduleResponse;
import com.very.relink.schedule.domain.ScheduleSlotStatus;
import com.very.relink.schedule.exception.ScheduleErrorCode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private static final int SLOT_MINUTES = 30;
    private static final int SLOTS_PER_DAY = 48;
    private static final int MINUTES_PER_DAY = 24 * 60;
    private static final int UPCOMING_STATUS_HOURS = 4;
    private static final int UPCOMING_STATUS_SLOT_COUNT = UPCOMING_STATUS_HOURS * 60 / SLOT_MINUTES;

    private final MemberJpaRepository memberJpaRepository;
    private final WeeklyScheduleJpaRepository weeklyScheduleJpaRepository;
    private final ScheduleSlotJpaRepository scheduleSlotJpaRepository;

    @Transactional(readOnly = true)
    public WeeklyScheduleResponse getWeeklySchedule(Long memberId, LocalDate date) {
        LocalDate weekStartDate = toWeekStartDate(date);
        Map<SlotKey, ScheduleSlotJpaEntity> savedSlots = weeklyScheduleJpaRepository
                .findByMember_IdAndWeekStartDate(memberId, weekStartDate)
                .map(schedule -> scheduleSlotJpaRepository.findByWeeklySchedule_IdOrderByScheduleDateAscStartTimeAsc(schedule.getId()))
                .map(this::toSlotMap)
                .orElseGet(Map::of);

        List<DailyScheduleResponse> days = new ArrayList<>();
        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            LocalDate scheduleDate = weekStartDate.plusDays(dayOffset);
            days.add(new DailyScheduleResponse(scheduleDate, buildDailySlots(scheduleDate, savedSlots)));
        }

        return new WeeklyScheduleResponse(weekStartDate, weekStartDate.plusDays(6), days);
    }

    @Transactional(readOnly = true)
    public UpcomingScheduleStatusResponse getUpcomingStatuses(List<Long> memberIds) {
        List<Long> requestedMemberIds = normalizeMemberIds(memberIds);
        validateMembersExist(requestedMemberIds);

        LocalDateTime from = toCurrentSlotStart(LocalDateTime.now());
        LocalDateTime to = from.plusHours(UPCOMING_STATUS_HOURS);
        Map<MemberSlotKey, ScheduleSlotJpaEntity> savedSlots = toMemberSlotMap(
                scheduleSlotJpaRepository.findAllByMemberIdsAndScheduleDateBetween(
                        requestedMemberIds,
                        from.toLocalDate(),
                        to.toLocalDate()
                )
        );

        return new UpcomingScheduleStatusResponse(
                from,
                to,
                requestedMemberIds.stream()
                        .map(memberId -> new MemberScheduleStatusResponse(
                                memberId,
                                buildUpcomingSlots(memberId, from, savedSlots)
                        ))
                        .toList()
        );
    }

    @Transactional
    public UpdateScheduleSlotsResponse updateSlots(UpdateScheduleSlotsCommand command) {
        if (command.slots() == null || command.slots().isEmpty()) {
            throw ScheduleErrorCode.INVALID_SCHEDULE_RANGE.toException();
        }

        List<ExpandedSlot> expandedSlots = command.slots().stream()
                .flatMap(slot -> expand(slot).stream())
                .sorted(Comparator
                        .comparing(ExpandedSlot::scheduleDate)
                        .thenComparing(ExpandedSlot::startTime))
                .toList();

        LocalDate weekStartDate = toWeekStartDate(expandedSlots.get(0).scheduleDate());
        validateSameWeek(weekStartDate, expandedSlots);

        WeeklyScheduleJpaEntity weeklySchedule = getOrCreateWeeklySchedule(command.memberId(), weekStartDate);
        List<ScheduleSlotChangeResponse> changedSlots = new ArrayList<>();

        for (ExpandedSlot expandedSlot : expandedSlots) {
            scheduleSlotJpaRepository
                    .findByWeeklySchedule_IdAndScheduleDateAndStartTimeAndEndTime(
                            weeklySchedule.getId(),
                            expandedSlot.scheduleDate(),
                            expandedSlot.startTime(),
                            expandedSlot.endTime()
                    )
                    .ifPresentOrElse(
                            slot -> updateExistingSlot(slot, expandedSlot.status(), changedSlots),
                            () -> createSlotIfNeeded(weeklySchedule, expandedSlot, changedSlots)
                    );
        }

        return new UpdateScheduleSlotsResponse(weekStartDate, changedSlots);
    }

    private WeeklyScheduleJpaEntity getOrCreateWeeklySchedule(Long memberId, LocalDate weekStartDate) {
        return weeklyScheduleJpaRepository.findByMember_IdAndWeekStartDate(memberId, weekStartDate)
                .orElseGet(() -> {
                    MemberJpaEntity member = memberJpaRepository.findById(memberId)
                            .orElseThrow(ScheduleErrorCode.MEMBER_NOT_FOUND::toException);
                    return weeklyScheduleJpaRepository.save(WeeklyScheduleJpaEntity.create(member, weekStartDate));
                });
    }

    private void updateExistingSlot(
            ScheduleSlotJpaEntity slot,
            ScheduleSlotStatus status,
            List<ScheduleSlotChangeResponse> changedSlots
    ) {
        if (status == ScheduleSlotStatus.AVAILABLE) {
            scheduleSlotJpaRepository.delete(slot);
            changedSlots.add(new ScheduleSlotChangeResponse(
                    slot.getScheduleDate(),
                    slot.getStartTime(),
                    slot.getEndTime(),
                    status
            ));
            return;
        }

        slot.updateStatus(status);
        changedSlots.add(new ScheduleSlotChangeResponse(
                slot.getScheduleDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                status
        ));
    }

    private void createSlotIfNeeded(
            WeeklyScheduleJpaEntity weeklySchedule,
            ExpandedSlot expandedSlot,
            List<ScheduleSlotChangeResponse> changedSlots
    ) {
        if (expandedSlot.status() == ScheduleSlotStatus.AVAILABLE) {
            changedSlots.add(new ScheduleSlotChangeResponse(
                    expandedSlot.scheduleDate(),
                    expandedSlot.startTime(),
                    expandedSlot.endTime(),
                    expandedSlot.status()
            ));
            return;
        }

        ScheduleSlotJpaEntity slot = scheduleSlotJpaRepository.save(ScheduleSlotJpaEntity.create(
                weeklySchedule,
                expandedSlot.scheduleDate(),
                expandedSlot.startTime(),
                expandedSlot.endTime(),
                expandedSlot.status()
        ));
        changedSlots.add(new ScheduleSlotChangeResponse(
                slot.getScheduleDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getStatus()
        ));
    }

    private List<ScheduleSlotResponse> buildDailySlots(
            LocalDate scheduleDate,
            Map<SlotKey, ScheduleSlotJpaEntity> savedSlots
    ) {
        List<ScheduleSlotResponse> slots = new ArrayList<>();
        for (int slotIndex = 0; slotIndex < SLOTS_PER_DAY; slotIndex++) {
            LocalTime startTime = LocalTime.MIDNIGHT.plusMinutes((long) slotIndex * SLOT_MINUTES);
            LocalTime endTime = startTime.plusMinutes(SLOT_MINUTES);
            ScheduleSlotJpaEntity savedSlot = savedSlots.get(new SlotKey(scheduleDate, startTime, endTime));
            slots.add(new ScheduleSlotResponse(
                    startTime,
                    endTime,
                    savedSlot == null ? ScheduleSlotStatus.AVAILABLE : savedSlot.getStatus(),
                    savedSlot == null || savedSlot.getAppointment() == null ? null : savedSlot.getAppointment().getId()
            ));
        }
        return slots;
    }

    private Map<SlotKey, ScheduleSlotJpaEntity> toSlotMap(List<ScheduleSlotJpaEntity> slots) {
        Map<SlotKey, ScheduleSlotJpaEntity> slotMap = new HashMap<>();
        for (ScheduleSlotJpaEntity slot : slots) {
            slotMap.put(new SlotKey(slot.getScheduleDate(), slot.getStartTime(), slot.getEndTime()), slot);
        }
        return slotMap;
    }

    private List<Long> normalizeMemberIds(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            throw ScheduleErrorCode.INVALID_MEMBER_IDS.toException();
        }

        Set<Long> uniqueMemberIds = new LinkedHashSet<>();
        for (Long memberId : memberIds) {
            if (memberId == null) {
                throw ScheduleErrorCode.INVALID_MEMBER_IDS.toException();
            }
            uniqueMemberIds.add(memberId);
        }
        return new ArrayList<>(uniqueMemberIds);
    }

    private void validateMembersExist(List<Long> memberIds) {
        if (memberJpaRepository.findAllById(memberIds).size() != memberIds.size()) {
            throw ScheduleErrorCode.MEMBER_NOT_FOUND.toException();
        }
    }

    private LocalDateTime toCurrentSlotStart(LocalDateTime dateTime) {
        int slotMinute = dateTime.getMinute() < SLOT_MINUTES ? 0 : SLOT_MINUTES;
        return dateTime.withMinute(slotMinute).withSecond(0).withNano(0);
    }

    private List<UpcomingScheduleSlotResponse> buildUpcomingSlots(
            Long memberId,
            LocalDateTime from,
            Map<MemberSlotKey, ScheduleSlotJpaEntity> savedSlots
    ) {
        List<UpcomingScheduleSlotResponse> slots = new ArrayList<>();
        for (int slotIndex = 0; slotIndex < UPCOMING_STATUS_SLOT_COUNT; slotIndex++) {
            LocalDateTime slotStart = from.plusMinutes((long) slotIndex * SLOT_MINUTES);
            LocalTime startTime = slotStart.toLocalTime();
            LocalTime endTime = startTime.plusMinutes(SLOT_MINUTES);
            ScheduleSlotJpaEntity savedSlot = savedSlots.get(new MemberSlotKey(
                    memberId,
                    slotStart.toLocalDate(),
                    startTime,
                    endTime
            ));

            slots.add(new UpcomingScheduleSlotResponse(
                    slotStart.toLocalDate(),
                    startTime,
                    endTime,
                    savedSlot == null ? ScheduleSlotStatus.AVAILABLE : savedSlot.getStatus(),
                    savedSlot == null || savedSlot.getAppointment() == null ? null : savedSlot.getAppointment().getId()
            ));
        }
        return slots;
    }

    private Map<MemberSlotKey, ScheduleSlotJpaEntity> toMemberSlotMap(List<ScheduleSlotJpaEntity> slots) {
        Map<MemberSlotKey, ScheduleSlotJpaEntity> slotMap = new HashMap<>();
        for (ScheduleSlotJpaEntity slot : slots) {
            slotMap.put(
                    new MemberSlotKey(
                            slot.getWeeklySchedule().getMember().getId(),
                            slot.getScheduleDate(),
                            slot.getStartTime(),
                            slot.getEndTime()
                    ),
                    slot
            );
        }
        return slotMap;
    }

    private List<ExpandedSlot> expand(ScheduleSlotUpdateCommand command) {
        if (command.scheduleDate() == null || command.startTime() == null
                || command.endTime() == null || command.status() == null) {
            throw ScheduleErrorCode.INVALID_SCHEDULE_RANGE.toException();
        }

        int startMinute = toMinuteOfDay(command.startTime());
        int endMinute = toRangeEndMinute(command.startTime(), command.endTime());
        if (startMinute >= endMinute || !isAligned(command.startTime()) || !isAligned(command.endTime())) {
            throw ScheduleErrorCode.INVALID_SCHEDULE_SLOT_UNIT.toException();
        }

        List<ExpandedSlot> slots = new ArrayList<>();
        for (int minute = startMinute; minute < endMinute; minute += SLOT_MINUTES) {
            LocalTime startTime = LocalTime.MIDNIGHT.plusMinutes(minute);
            LocalTime endTime = startTime.plusMinutes(SLOT_MINUTES);
            slots.add(new ExpandedSlot(command.scheduleDate(), startTime, endTime, command.status()));
        }
        return slots;
    }

    private int toMinuteOfDay(LocalTime time) {
        return time.getHour() * 60 + time.getMinute();
    }

    private boolean isAligned(LocalTime time) {
        return time.getMinute() % SLOT_MINUTES == 0 && time.getSecond() == 0 && time.getNano() == 0;
    }

    private int toRangeEndMinute(LocalTime startTime, LocalTime endTime) {
        int startMinute = toMinuteOfDay(startTime);
        int endMinute = toMinuteOfDay(endTime);
        if (endMinute == 0 && startMinute > 0) {
            return MINUTES_PER_DAY;
        }
        return endMinute;
    }

    private LocalDate toWeekStartDate(LocalDate date) {
        if (date == null) {
            throw ScheduleErrorCode.INVALID_SCHEDULE_RANGE.toException();
        }
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private void validateSameWeek(LocalDate weekStartDate, List<ExpandedSlot> slots) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        boolean invalid = slots.stream()
                .map(ExpandedSlot::scheduleDate)
                .anyMatch(date -> date.isBefore(weekStartDate) || date.isAfter(weekEndDate));
        if (invalid) {
            throw ScheduleErrorCode.INVALID_SCHEDULE_RANGE.toException();
        }
    }

    private record SlotKey(
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
    }

    private record MemberSlotKey(
            Long memberId,
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
    }

    private record ExpandedSlot(
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime,
            ScheduleSlotStatus status
    ) {
    }
}
