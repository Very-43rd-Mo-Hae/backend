package com.very.relink.auth.application.port.out;

import com.very.relink.auth.domain.session.AuthSession;

public interface SaveAuthSessionPort {

    AuthSession save(AuthSession authSession);
}
