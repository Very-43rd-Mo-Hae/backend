package com.very.relink.chat.infrastructure.outbox;

import com.very.relink.chat.application.port.ChatPorts.ChatMessagePublisher;
import com.very.relink.chat.application.response.ChatResponses.ChatMessageCreatedPayload;
import com.very.relink.chat.application.response.ChatResponses.ChatRoomReadPayload;
import com.very.relink.chat.domain.ChatEnums.OutboxStatus;
import com.very.relink.chat.infrastructure.config.ChatProperties;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatOutboxEventJpaEntity;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatOutboxEventJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatOutboxRelay {

    private static final int MAX_RETRY_COUNT = 3;

    private final ChatOutboxEventJpaRepository chatOutboxEventJpaRepository;
    private final ChatMessagePublisher chatMessagePublisher;
    private final ChatProperties chatProperties;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${chat.outbox.fixed-delay-millis:1000}")
    @Transactional
    public void publishPendingEvents() {
        if (!chatProperties.outbox().enabled()) {
            return;
        }

        findPendingEvents().forEach(this::publishEvent);
    }

    public List<ChatOutboxEventJpaEntity> findPendingEvents() {
        return chatOutboxEventJpaRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
    }

    private void publishEvent(ChatOutboxEventJpaEntity event) {
        try {
            switch (event.getEventType()) {
                case CHAT_MESSAGE_CREATED -> chatMessagePublisher.publishMessageCreated(readPayload(event, ChatMessageCreatedPayload.class));
                case CHAT_ROOM_READ -> chatMessagePublisher.publishRoomRead(readPayload(event, ChatRoomReadPayload.class));
            }
            event.markPublished(LocalDateTime.now());
        } catch (RuntimeException ex) {
            event.recordPublishFailure(MAX_RETRY_COUNT);
            log.warn("Chat outbox publish failed. eventId={}, eventType={}, retryCount={}",
                    event.getId(), event.getEventType(), event.getRetryCount(), ex);
        }
    }

    private <T> T readPayload(ChatOutboxEventJpaEntity event, Class<T> payloadType) {
        try {
            return objectMapper.readValue(event.getPayloadJson(), payloadType);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Failed to deserialize chat outbox payload. eventId=" + event.getId(), ex);
        }
    }
}
