package com.very.relink.auth.application.port.out;

public interface GetRefreshTokenCachePort {

    String getTokenBySessionId(String sessionId);
}
