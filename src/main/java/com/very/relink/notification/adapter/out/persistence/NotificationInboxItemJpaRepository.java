package com.very.relink.notification.adapter.out.persistence;

import com.very.relink.notification.domain.model.NotificationInboxStatus;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationInboxItemJpaRepository extends JpaRepository<NotificationInboxItemJpaEntity, Long> {

    @EntityGraph(attributePaths = "notification")
    Page<NotificationInboxItemJpaEntity> findByMember_IdAndStatusNotOrderByCreatedAtDesc(
            Long memberId,
            NotificationInboxStatus status,
            Pageable pageable
    );

    Optional<NotificationInboxItemJpaEntity> findByIdAndMember_IdAndStatusNot(
            Long id,
            Long memberId,
            NotificationInboxStatus status
    );

    boolean existsByMember_IdAndStatus(Long memberId, NotificationInboxStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update NotificationInboxItemJpaEntity item
            set item.status = com.very.relink.notification.domain.model.NotificationInboxStatus.READ,
                item.readAt = CURRENT_TIMESTAMP
            where item.member.id = :memberId
              and item.status = com.very.relink.notification.domain.model.NotificationInboxStatus.UNREAD
            """)
    int markAllReadByMemberId(@Param("memberId") Long memberId);
}
