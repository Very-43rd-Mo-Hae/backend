package com.very.relink.auth.adapter.out.token;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class BCryptRefreshTokenHashAdapterTest {

    private final BCryptRefreshTokenHashAdapter adapter = new BCryptRefreshTokenHashAdapter(
            new BCryptPasswordEncoder()
    );

    @Test
    @DisplayName("72 bytes longer refresh token can be hashed and matched.")
    void hashAndMatchesLongRefreshToken() {
        String refreshToken = "eyJhbGciOiJIUzI1NiJ9."
                + "eyJzdWIiOiIxIiwidHlwZSI6IlJFRlJFU0giLCJzZXNzaW9uSWQiOiJzZXNzaW9uLWlkIiwianRpIjoicmVmcmVzaC10b2tlbi1qdGkifQ."
                + "signature-signature-signature";

        String refreshTokenHash = adapter.hash(refreshToken);

        assertThat(refreshTokenHash).isNotEqualTo(refreshToken);
        assertThat(adapter.matches(refreshToken, refreshTokenHash)).isTrue();
        assertThat(adapter.matches(refreshToken + "-other", refreshTokenHash)).isFalse();
    }
}
