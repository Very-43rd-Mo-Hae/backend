package com.very.relink.notification.domain.model;

public enum NotificationOutboxStatus {
    PENDING,
    PROCESSING,
    SENT,
    FAILED,
    SKIPPED
}
