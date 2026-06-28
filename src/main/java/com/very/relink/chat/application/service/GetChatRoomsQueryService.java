package com.very.relink.chat.application.service;

import com.very.relink.chat.application.port.ChatPorts.StorageUrlResolver;
import com.very.relink.chat.application.query.ChatQueryPort;
import com.very.relink.chat.application.response.ChatResponses.ChatRoomSummaryResponse;
import com.very.relink.chat.application.response.ChatResponses.ChatRoomsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetChatRoomsQueryService {

    private final ChatQueryPort chatQueryPort;
    private final StorageUrlResolver storageUrlResolver;

    @Transactional(readOnly = true)
    public ChatRoomsResponse getRooms(Long memberId) {
        return new ChatRoomsResponse(chatQueryPort.findRooms(memberId).stream()
                .map(room -> new ChatRoomSummaryResponse(
                        room.roomId(),
                        room.roomType(),
                        room.title(),
                        room.displayName(),
                        room.coverImageKey() == null ? null : storageUrlResolver.resolveUrl(room.coverImageKey()),
                        room.lastMessage(),
                        room.lastMessageType(),
                        room.lastMessageAt(),
                        room.unreadCount()
                ))
                .toList());
    }
}
