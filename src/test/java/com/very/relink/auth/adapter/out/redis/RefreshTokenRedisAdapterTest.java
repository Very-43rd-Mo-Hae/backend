package com.very.relink.auth.adapter.out.redis;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SuppressWarnings("unchecked")
class RefreshTokenRedisAdapterTest {

    @Test
    @DisplayName("refresh token hash를 session id 기반 Redis key에 TTL과 함께 저장한다.")
    void saveRefreshTokenHashWithTtl() {
        RedisTemplate<String, Object> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        RefreshTokenRedisAdapter adapter = new RefreshTokenRedisAdapter(redisTemplate);

        adapter.save("session-id", "refresh-token-hash", Duration.ofSeconds(1209600L));

        verify(valueOperations).set(
                "refresh:session-id",
                "refresh-token-hash",
                Duration.ofSeconds(1209600L)
        );
    }
}
