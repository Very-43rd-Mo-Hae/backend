package com.very.relink.notification.application.service;

import com.very.relink.notification.infrastructure.config.NotificationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationOutboxScheduler {

    private final JobOperator jobOperator;
    private final Job webPushOutboxJob;
    private final NotificationProperties notificationProperties;

    @Scheduled(fixedDelayString = "${notification.outbox.fixed-delay-millis:5000}")
    public void processWebPushOutbox() {
        if (!notificationProperties.outbox().enabled()) {
            return;
        }

        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("requestedAt", System.currentTimeMillis())
                    .toJobParameters();
            jobOperator.start(webPushOutboxJob, jobParameters);
        } catch (Exception ex) {
            log.warn("Notification outbox batch failed.", ex);
        }
    }
}
