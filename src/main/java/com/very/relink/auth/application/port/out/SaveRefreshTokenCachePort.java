package com.very.relink.auth.application.port.out;

import java.time.Duration;

public interface SaveRefreshTokenCachePort {

    void save(String sessionId, String refreshTokenHash, Duration ttl);
}
