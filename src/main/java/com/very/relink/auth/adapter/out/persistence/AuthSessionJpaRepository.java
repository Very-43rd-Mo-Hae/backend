package com.very.relink.auth.adapter.out.persistence;

import com.very.relink.auth.domain.session.AuthSessionStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthSessionJpaRepository extends JpaRepository<AuthSessionJpaEntity, Long> {

    Optional<AuthSessionJpaEntity> findBySessionId(String sessionId);

    List<AuthSessionJpaEntity> findAllByMember_IdAndStatus(Long memberId, AuthSessionStatus status);
}
