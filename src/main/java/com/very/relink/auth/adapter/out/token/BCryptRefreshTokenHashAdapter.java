package com.very.relink.auth.adapter.out.token;

import com.very.relink.auth.application.port.out.RefreshTokenHashPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BCryptRefreshTokenHashAdapter implements RefreshTokenHashPort {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public String hash(String refreshToken) {
        return bCryptPasswordEncoder.encode(refreshToken);
    }

    @Override
    public boolean matches(String refreshToken, String refreshTokenHash) {
        return bCryptPasswordEncoder.matches(refreshToken, refreshTokenHash);
    }
}
