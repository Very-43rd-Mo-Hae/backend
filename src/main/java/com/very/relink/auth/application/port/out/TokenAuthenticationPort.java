package com.very.relink.auth.application.port.out;

import com.very.relink.auth.domain.token.AuthenticatedMember;

public interface TokenAuthenticationPort {

    AuthenticatedMember authenticate(String accessToken);
}
