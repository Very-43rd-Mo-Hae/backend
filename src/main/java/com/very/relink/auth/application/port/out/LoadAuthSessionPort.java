package com.very.relink.auth.application.port.out;

import com.very.relink.auth.domain.session.AuthSession;
import com.very.relink.auth.domain.session.AuthSessionStatus;
import java.util.List;
import java.util.Optional;

public interface LoadAuthSessionPort {

    Optional<AuthSession> findBySessionId(String sessionId);

    List<AuthSession> findAllByMemberIdAndStatus(Long memberId, AuthSessionStatus status);
}
