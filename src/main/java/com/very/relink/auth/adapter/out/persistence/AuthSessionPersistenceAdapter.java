package com.very.relink.auth.adapter.out.persistence;

import com.very.relink.auth.application.port.out.LoadAuthSessionPort;
import com.very.relink.auth.application.port.out.SaveAuthSessionPort;
import com.very.relink.auth.domain.session.AuthSession;
import com.very.relink.auth.domain.session.AuthSessionStatus;
import com.very.relink.member.adapter.out.persistence.MemberJpaEntity;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthSessionPersistenceAdapter implements LoadAuthSessionPort, SaveAuthSessionPort {

    private final AuthSessionJpaRepository authSessionJpaRepository;
    private final AuthSessionMapper authSessionMapper;
    private final EntityManager entityManager;

    @Override
    public Optional<AuthSession> findBySessionId(String sessionId) {
        return authSessionJpaRepository.findBySessionId(sessionId)
                .map(authSessionMapper::toDomain);
    }

    @Override
    public List<AuthSession> findAllByMemberIdAndStatus(Long memberId, AuthSessionStatus status) {
        return authSessionJpaRepository.findAllByMember_IdAndStatus(memberId, status)
                .stream()
                .map(authSessionMapper::toDomain)
                .toList();
    }

    @Override
    public AuthSession save(AuthSession authSession) {
        MemberJpaEntity member = entityManager.getReference(MemberJpaEntity.class, authSession.getMemberId());
        AuthSessionJpaEntity entity = authSessionMapper.toEntity(authSession, member);
        AuthSessionJpaEntity savedEntity = authSessionJpaRepository.save(entity);
        return authSessionMapper.toDomain(savedEntity);
    }
}
