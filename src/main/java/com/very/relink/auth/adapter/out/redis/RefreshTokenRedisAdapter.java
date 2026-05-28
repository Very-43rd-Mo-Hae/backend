package com.very.relink.auth.adapter.out.redis;

import com.very.relink.auth.application.port.out.SaveRefreshTokenCachePort;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenRedisAdapter implements SaveRefreshTokenCachePort {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(String sessionId, String refreshTokenHash, Duration ttl) {
        redisTemplate.opsForValue()
                .set(REFRESH_TOKEN_KEY_PREFIX + sessionId, refreshTokenHash, ttl);
    }
}
