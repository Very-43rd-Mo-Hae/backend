package com.very.relink.notification.application.port.out;

import com.very.relink.notification.domain.model.NotificationChannel;
import java.time.Duration;

public interface NotificationSendDeduplicationPort {

    boolean acquireSendLock(Long userId, Long notificationId, NotificationChannel channel, Duration ttl);
}
