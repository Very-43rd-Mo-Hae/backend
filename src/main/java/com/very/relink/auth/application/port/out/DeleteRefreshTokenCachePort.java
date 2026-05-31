package com.very.relink.auth.application.port.out;

public interface DeleteRefreshTokenCachePort {

    void deleteBySessionId(String sessionId);
}
