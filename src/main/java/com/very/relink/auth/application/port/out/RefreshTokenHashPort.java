package com.very.relink.auth.application.port.out;

public interface RefreshTokenHashPort {

    String hash(String refreshToken);

    boolean matches(String refreshToken, String refreshTokenHash);
}
