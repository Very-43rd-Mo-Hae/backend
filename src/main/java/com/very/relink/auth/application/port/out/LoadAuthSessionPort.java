package com.very.relink.auth.application.port.out;

import com.very.relink.auth.domain.session.AuthSession;
import java.util.Optional;

public interface LoadAuthSessionPort {

    Optional<AuthSession> findBySessionId(String sessionId);
}
