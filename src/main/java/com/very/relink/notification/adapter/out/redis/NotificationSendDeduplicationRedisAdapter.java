package com.very.relink.notification.adapter.out.redis;

import com.very.relink.notification.application.port.out.NotificationSendDeduplicationPort;
import com.very.relink.notification.domain.model.NotificationChannel;
import com.very.relink.notification.infrastructure.config.NotificationProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSendDeduplicationRedisAdapter implements NotificationSendDeduplicationPort {

    private final RedisTemplate<String, Object> redisTemplate;
    private final NotificationProperties notificationProperties;

    @Override
    public boolean acquireSendLock(Long userId, Long notificationId, NotificationChannel channel, Duration ttl) {
        String key = "%s:%s:%s:%s".formatted(
                notificationProperties.redis().dedupPrefix(),
                channel.name().toLowerCase(),
                userId,
                notificationId
        );

        try {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
            return Boolean.TRUE.equals(acquired);
        } catch (RuntimeException ex) {
            log.warn("Web push deduplication failed. Continue sending. userId={}, notificationId={}",
                    userId, notificationId, ex);
            return true;
        }
    }
}
