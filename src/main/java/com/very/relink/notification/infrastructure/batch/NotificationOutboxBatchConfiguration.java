package com.very.relink.notification.infrastructure.batch;

import com.very.relink.notification.application.service.WebPushNotificationService;
import com.very.relink.notification.infrastructure.config.NotificationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class NotificationOutboxBatchConfiguration {

    public static final String WEB_PUSH_OUTBOX_JOB = "webPushOutboxJob";
    public static final String WEB_PUSH_OUTBOX_STEP = "webPushOutboxStep";

    private final WebPushNotificationService webPushNotificationService;
    private final NotificationProperties notificationProperties;

    @Bean
    public Job webPushOutboxJob(JobRepository jobRepository, Step webPushOutboxStep) {
        return new JobBuilder(WEB_PUSH_OUTBOX_JOB, jobRepository)
                .start(webPushOutboxStep)
                .build();
    }

    @Bean
    public Step webPushOutboxStep(
            JobRepository jobRepository,
            @Qualifier("transactionManager") PlatformTransactionManager transactionManager,
            Tasklet webPushOutboxTasklet
    ) {
        return new StepBuilder(WEB_PUSH_OUTBOX_STEP, jobRepository)
                .tasklet(webPushOutboxTasklet, transactionManager)
                .build();
    }

    @Bean
    public Tasklet webPushOutboxTasklet() {
        return (contribution, chunkContext) -> {
            webPushNotificationService.processPendingOutboxes(notificationProperties.outbox().batchSize());
            return RepeatStatus.FINISHED;
        };
    }
}
