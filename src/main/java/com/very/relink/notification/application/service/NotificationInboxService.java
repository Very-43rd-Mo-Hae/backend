package com.very.relink.notification.application.service;

import com.very.relink.notification.adapter.out.persistence.NotificationInboxItemJpaEntity;
import com.very.relink.notification.adapter.out.persistence.NotificationInboxItemJpaRepository;
import com.very.relink.notification.adapter.out.persistence.NotificationJpaEntity;
import com.very.relink.notification.application.response.NotificationInboxResponses.NotificationInboxItemResponse;
import com.very.relink.notification.application.response.NotificationInboxResponses.NotificationInboxResponse;
import com.very.relink.notification.domain.model.NotificationInboxStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class NotificationInboxService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 50;

    private final NotificationInboxItemJpaRepository notificationInboxItemJpaRepository;

    @Transactional(readOnly = true)
    public NotificationInboxResponse getNotifications(Long memberId, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = normalizeSize(size);
        Page<NotificationInboxItemJpaEntity> notifications = notificationInboxItemJpaRepository
                .findByMember_IdAndStatusNotOrderByCreatedAtDesc(
                        memberId,
                        NotificationInboxStatus.DELETED,
                        PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"))
                );

        return new NotificationInboxResponse(
                notifications.stream().map(this::toResponse).toList(),
                notifications.getNumber(),
                notifications.getSize(),
                notifications.hasNext(),
                notificationInboxItemJpaRepository.existsByMember_IdAndStatus(memberId, NotificationInboxStatus.UNREAD)
        );
    }

    @Transactional
    public void markAllRead(Long memberId) {
        notificationInboxItemJpaRepository.markAllReadByMemberId(memberId);
    }

    @Transactional
    public void deleteNotification(Long memberId, Long notificationInboxItemId) {
        NotificationInboxItemJpaEntity notification = notificationInboxItemJpaRepository
                .findByIdAndMember_IdAndStatusNot(notificationInboxItemId, memberId, NotificationInboxStatus.DELETED)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        notification.delete();
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }

        return Math.min(size, MAX_PAGE_SIZE);
    }

    private NotificationInboxItemResponse toResponse(NotificationInboxItemJpaEntity inboxItem) {
        NotificationJpaEntity notification = inboxItem.getNotification();

        return new NotificationInboxItemResponse(
                inboxItem.getId(),
                notification.getId(),
                notification.getTitle(),
                notification.getBody(),
                notification.getLinkUrl(),
                inboxItem.getStatus() == NotificationInboxStatus.READ,
                inboxItem.getCreatedAt(),
                inboxItem.getReadAt()
        );
    }
}
