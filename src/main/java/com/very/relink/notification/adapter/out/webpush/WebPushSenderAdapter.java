package com.very.relink.notification.adapter.out.webpush;

import com.very.relink.notification.application.dto.NotificationTargetProjection;
import com.very.relink.notification.application.dto.WebPushPayload;
import com.very.relink.notification.application.port.out.WebPushSenderPort;
import com.very.relink.notification.infrastructure.config.NotificationProperties;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import org.apache.http.HttpResponse;
import org.jose4j.lang.JoseException;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebPushSenderAdapter implements WebPushSenderPort {

    private final ObjectMapper objectMapper;
    private final NotificationProperties notificationProperties;

    @Override
    public WebPushSendResponse send(NotificationTargetProjection target, WebPushPayload payload) {
        if (!notificationProperties.webPush().enabled()) {
            return new WebPushSendResponse(false, false);
        }

        try {
            PushService pushService = new PushService(
                    notificationProperties.webPush().publicKey(),
                    notificationProperties.webPush().privateKey(),
                    notificationProperties.webPush().subject()
            );
            Subscription subscription = new Subscription(
                    target.endpoint(),
                    new Subscription.Keys(target.p256dh(), target.auth())
            );
            Notification notification = new Notification(subscription, objectMapper.writeValueAsString(payload));
            HttpResponse response = pushService.send(notification);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode >= 200 && statusCode < 300) {
                return new WebPushSendResponse(true, false);
            }

            boolean expired = statusCode == 404 || statusCode == 410;
            log.warn("Web push sending failed. targetId={}, statusCode={}", target.id(), statusCode);
            return new WebPushSendResponse(false, expired);
        } catch (JacksonException ex) {
            log.error("Web push payload serialization failed. targetId={}", target.id(), ex);
            return new WebPushSendResponse(false, false);
        } catch (GeneralSecurityException | JoseException | IOException | ExecutionException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.warn("Web push sending failed. targetId={}", target.id(), ex);
            return new WebPushSendResponse(false, false);
        }
    }
}
