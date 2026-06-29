package com.very.relink.friend.adapter.out.redis;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FriendLightningRedisAdapter {

    private static final String LIGHTNING_KEY_PREFIX = "friend:lightning:";
    private static final Duration LIGHTNING_TTL = Duration.ofMinutes(3);

    private final RedisTemplate<String, Object> redisTemplate;

    public void refreshActive(Long memberId) {
        redisTemplate.opsForValue().set(toKey(memberId), true, LIGHTNING_TTL);
    }

    public Map<Long, Boolean> getActiveMap(Collection<Long> memberIds) {
        return memberIds.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        memberId -> Boolean.TRUE.equals(redisTemplate.hasKey(toKey(memberId)))
                ));
    }

    private String toKey(Long memberId) {
        return LIGHTNING_KEY_PREFIX + memberId;
    }
}
