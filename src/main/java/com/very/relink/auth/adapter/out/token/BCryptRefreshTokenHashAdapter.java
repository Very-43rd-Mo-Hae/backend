package com.very.relink.auth.adapter.out.token;

import com.very.relink.auth.application.port.out.RefreshTokenHashPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BCryptRefreshTokenHashAdapter implements RefreshTokenHashPort {

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public String hash(String refreshToken) {
        return bCryptPasswordEncoder.encode(digest(refreshToken));
    }

    @Override
    public boolean matches(String refreshToken, String refreshTokenHash) {
        return bCryptPasswordEncoder.matches(digest(refreshToken), refreshTokenHash);
    }

    private String digest(String refreshToken) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] digest = messageDigest.digest(refreshToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available.", exception);
        }
    }
}
