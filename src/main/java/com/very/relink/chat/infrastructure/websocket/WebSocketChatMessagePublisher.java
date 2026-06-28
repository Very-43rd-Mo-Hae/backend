package com.very.relink.chat.infrastructure.websocket;

import com.very.relink.chat.application.port.ChatPorts.ChatMessagePublisher;
import com.very.relink.chat.application.response.ChatResponses.ChatMessageCreatedPayload;
import com.very.relink.chat.application.response.ChatResponses.ChatRoomReadPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketChatMessagePublisher implements ChatMessagePublisher {

    @Override
    public void publishMessageCreated(ChatMessageCreatedPayload payload) {
        log.debug("chat message created publish stub. roomId={}, messageId={}", payload.roomId(), payload.messageId());
    }

    @Override
    public void publishRoomRead(ChatRoomReadPayload payload) {
        log.debug("chat room read publish stub. roomId={}, memberId={}", payload.roomId(), payload.memberId());
    }
}
