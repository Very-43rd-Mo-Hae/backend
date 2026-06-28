package com.very.relink.notification.application.port.out;

import com.very.relink.notification.application.dto.NotificationTargetProjection;
import com.very.relink.notification.application.dto.WebPushPayload;

public interface WebPushSenderPort {

    WebPushSendResponse send(NotificationTargetProjection target, WebPushPayload payload);

    record WebPushSendResponse(boolean success, boolean expired) {
    }
}
