package com.very.relink.chat.application.service;

import com.very.relink.chat.application.command.ChatCommands.CreateDirectChatRoomCommand;
import com.very.relink.chat.application.response.ChatResponses.CreateChatRoomResponse;
import com.very.relink.chat.domain.ChatEnums.ParticipantRole;
import com.very.relink.chat.domain.ChatEnums.RoomType;
import com.very.relink.chat.exception.ChatErrorCode;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatParticipantJpaEntity;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatParticipantJpaRepository;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatRoomJpaEntity;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatRoomJpaRepository;
import com.very.relink.chat.infrastructure.persistence.jpa.DirectChatRoomJpaEntity;
import com.very.relink.chat.infrastructure.persistence.jpa.DirectChatRoomJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateDirectChatRoomService {

    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final DirectChatRoomJpaRepository directChatRoomJpaRepository;
    private final ChatParticipantJpaRepository chatParticipantJpaRepository;

    @Transactional
    public CreateChatRoomResponse create(CreateDirectChatRoomCommand command) {
        if (command.requesterId().equals(command.targetMemberId())) {
            throw ChatErrorCode.CANNOT_CREATE_DIRECT_ROOM_WITH_SELF.toException();
        }

        long low = Math.min(command.requesterId(), command.targetMemberId());
        long high = Math.max(command.requesterId(), command.targetMemberId());
        return directChatRoomJpaRepository.findByMemberLowIdAndMemberHighId(low, high)
                .map(room -> new CreateChatRoomResponse(room.getRoomId(), RoomType.DIRECT))
                .orElseGet(() -> createNewRoom(command));
    }

    private CreateChatRoomResponse createNewRoom(CreateDirectChatRoomCommand command) {
        ChatRoomJpaEntity room = chatRoomJpaRepository.save(ChatRoomJpaEntity.createDirect());
        directChatRoomJpaRepository.save(DirectChatRoomJpaEntity.create(room.getId(), command.requesterId(), command.targetMemberId()));
        chatParticipantJpaRepository.save(ChatParticipantJpaEntity.active(room.getId(), command.requesterId(), ParticipantRole.MEMBER));
        chatParticipantJpaRepository.save(ChatParticipantJpaEntity.active(room.getId(), command.targetMemberId(), ParticipantRole.MEMBER));
        return new CreateChatRoomResponse(room.getId(), RoomType.DIRECT);
    }
}
