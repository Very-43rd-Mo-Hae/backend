package com.very.relink.member.adapter.out.persistence;

import com.very.relink.auth.domain.value.OAuth2Provider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, Long> {

    Optional<MemberJpaEntity> findByEmail(String email);
    Optional<MemberJpaEntity> findByProviderAndProviderId(
            OAuth2Provider provider, String providerId
    );
}
