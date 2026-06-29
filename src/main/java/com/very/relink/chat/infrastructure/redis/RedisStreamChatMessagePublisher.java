package com.very.relink.chat.infrastructure.redis;

import com.very.relink.chat.application.port.ChatPorts.ChatMessagePublisher;
import com.very.relink.chat.application.response.ChatResponses.ChatMessageCreatedPayload;
import com.very.relink.chat.application.response.ChatResponses.ChatRoomReadPayload;
import com.very.relink.chat.domain.ChatEnums.OutboxEventType;
import com.very.relink.chat.infrastructure.config.ChatProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStreamChatMessagePublisher implements ChatMessagePublisher {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatProperties chatProperties;
    private final ObjectMapper objectMapper;

    @Override
    public void publishMessageCreated(ChatMessageCreatedPayload payload) {
        publish(OutboxEventType.CHAT_MESSAGE_CREATED, payload.roomId(), payload.messageId(), payload);
    }

    @Override
    public void publishRoomRead(ChatRoomReadPayload payload) {
        publish(OutboxEventType.CHAT_ROOM_READ, payload.roomId(), payload.lastReadMessageId(), payload);
    }

    private void publish(OutboxEventType eventType, Long roomId, Long aggregateId, Object payload) {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("eventType", eventType.name());
        fields.put("roomId", String.valueOf(roomId));
        fields.put("aggregateId", String.valueOf(aggregateId));
        fields.put("payload", toJson(payload));

        RecordId recordId = redisTemplate.opsForStream()
                .add(StreamRecords.newRecord()
                        .ofMap(fields)
                        .withStreamKey(chatProperties.redis().streamKey()));

        log.debug("Chat event published to Redis Stream. streamKey={}, recordId={}, eventType={}, roomId={}",
                chatProperties.redis().streamKey(), recordId, eventType, roomId);
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Failed to serialize chat stream payload.", ex);
        }
    }
}
