package com.very.relink.member.application.port.out;

import com.very.relink.auth.domain.value.OAuth2Provider;
import com.very.relink.member.domain.Member;
import java.util.Optional;

public interface LoadMemberPort {

    Optional<Member> findById(Long id);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByProviderAndProviderId(
            OAuth2Provider provider,
            String providerId
    );
}
