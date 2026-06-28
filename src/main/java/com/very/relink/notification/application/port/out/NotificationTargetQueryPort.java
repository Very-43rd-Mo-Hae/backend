package com.very.relink.notification.application.port.out;

import com.very.relink.notification.application.dto.NotificationTargetProjection;
import java.util.List;

public interface NotificationTargetQueryPort {

    List<NotificationTargetProjection> findActiveTargetsByUserId(Long userId);
}
