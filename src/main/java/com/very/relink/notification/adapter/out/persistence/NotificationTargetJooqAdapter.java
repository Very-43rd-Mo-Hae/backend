package com.very.relink.notification.adapter.out.persistence;

import com.very.relink.notification.application.dto.NotificationTargetProjection;
import com.very.relink.notification.application.port.out.NotificationTargetQueryPort;
import com.very.relink.notification.domain.model.NotificationChannel;
import com.very.relink.notification.domain.model.NotificationTargetStatus;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Component;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@Component
@RequiredArgsConstructor
public class NotificationTargetJooqAdapter implements NotificationTargetQueryPort {

    private final DSLContext dslContext;

    @Override
    public List<NotificationTargetProjection> findActiveTargetsByUserId(Long userId) {
        return dslContext
                .select(
                        field("notification_target_id", Long.class),
                        field("user_id", Long.class),
                        field("endpoint", String.class),
                        field("p256dh", String.class),
                        field("auth", String.class)
                )
                .from(table("notification_target"))
                .where(field("user_id", Long.class).eq(userId))
                .and(field("channel", String.class).eq(NotificationChannel.WEB_PUSH.name()))
                .and(field("status", String.class).eq(NotificationTargetStatus.ACTIVE.name()))
                .fetch(record -> new NotificationTargetProjection(
                        record.get(field("notification_target_id", Long.class)),
                        record.get(field("user_id", Long.class)),
                        record.get(field("endpoint", String.class)),
                        record.get(field("p256dh", String.class)),
                        record.get(field("auth", String.class))
                ));
    }
}
