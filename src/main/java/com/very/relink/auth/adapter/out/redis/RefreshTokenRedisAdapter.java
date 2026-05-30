package com.very.relink.auth.adapter.out.redis;

import com.very.relink.auth.application.port.out.GetRefreshTokenCachePort;
import com.very.relink.auth.application.port.out.SaveRefreshTokenCachePort;
import java.time.Duration;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenRedisAdapter implements SaveRefreshTokenCachePort, GetRefreshTokenCachePort {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(String sessionId, String refreshTokenHash, Duration ttl) {
        redisTemplate.opsForValue()
                .set(REFRESH_TOKEN_KEY_PREFIX + sessionId, refreshTokenHash, ttl);
    }

    @Override
    public String getTokenBySessionId(String sessionId) {
        Object result = redisTemplate.opsForValue().get(REFRESH_TOKEN_KEY_PREFIX + sessionId);

        if(result == null) {
            return null;
        }

        return result.toString();
    }
}
