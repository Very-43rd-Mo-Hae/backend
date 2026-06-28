package com.very.relink.notification.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationMessage {

    private final Long id;
    private Long userId;
    private String title;
    private String body;
    private String linkUrl;
    private Long deduplicationId;
    private NotificationStatus status;

    public static NotificationMessage requested(
            Long userId,
            String title,
            String body,
            String linkUrl,
            Long deduplicationId
    ) {
        return NotificationMessage.builder()
                .userId(userId)
                .title(title)
                .body(body)
                .linkUrl(linkUrl)
                .deduplicationId(deduplicationId)
                .status(NotificationStatus.REQUESTED)
                .build();
    }
}
