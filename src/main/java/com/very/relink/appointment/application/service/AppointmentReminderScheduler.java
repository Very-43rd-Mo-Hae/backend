package com.very.relink.appointment.application.service;

import com.very.relink.appointment.adapter.out.persistence.AppointmentReminderJpaEntity;
import com.very.relink.appointment.adapter.out.persistence.AppointmentReminderJpaRepository;
import com.very.relink.notification.application.command.SendWebPushNotificationCommand;
import com.very.relink.notification.application.service.WebPushNotificationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentReminderScheduler {

    private static final int BATCH_SIZE = 100;

    private final AppointmentReminderJpaRepository appointmentReminderJpaRepository;
    private final WebPushNotificationService webPushNotificationService;

    @Scheduled(fixedDelayString = "${appointment.reminder.fixed-delay-millis:60000}")
    @Transactional
    public void sendDueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<AppointmentReminderJpaEntity> reminders =
                appointmentReminderJpaRepository.findBySentAtIsNullAndRemindAtLessThanEqualOrderByRemindAtAsc(
                        now,
                        PageRequest.of(0, BATCH_SIZE)
                );

        for (AppointmentReminderJpaEntity reminder : reminders) {
            try {
                webPushNotificationService.send(new SendWebPushNotificationCommand(
                        reminder.getMember().getId(),
                        reminderTitle(reminder.getMinutesBefore()),
                        reminder.getAppointment().getTitle() + " starts soon.",
                        "/calendar",
                        null,
                        Map.of(
                                "type", "APPOINTMENT_REMINDER",
                                "appointmentId", reminder.getAppointment().getId(),
                                "minutesBefore", reminder.getMinutesBefore()
                        )
                ));
                reminder.markSent(now);
            } catch (Exception ex) {
                log.warn("Appointment reminder enqueue failed. reminderId={}", reminder.getId(), ex);
            }
        }
    }

    private String reminderTitle(int minutesBefore) {
        if (minutesBefore >= 24 * 60) {
            return "Appointment tomorrow";
        }
        if (minutesBefore >= 3 * 60) {
            return "Appointment in 3 hours";
        }
        return "Appointment in 1 hour";
    }
}
