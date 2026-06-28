package com.very.relink.chat.application.service;

import com.very.relink.chat.application.command.ChatCommands.CreateGroupChatRoomCommand;
import com.very.relink.chat.application.response.ChatResponses.CreateChatRoomResponse;
import com.very.relink.chat.domain.ChatEnums.ParticipantRole;
import com.very.relink.chat.domain.ChatEnums.RoomType;
import com.very.relink.chat.exception.ChatErrorCode;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatParticipantJpaEntity;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatParticipantJpaRepository;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatRoomJpaEntity;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatRoomJpaRepository;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateGroupChatRoomService {

    private final ChatRoomJpaRepository chatRoomJpaRepository;
    private final ChatParticipantJpaRepository chatParticipantJpaRepository;

    @Transactional
    public CreateChatRoomResponse create(CreateGroupChatRoomCommand command) {
        if (command.title() == null || command.title().isBlank()) {
            throw ChatErrorCode.INVALID_GROUP_PARTICIPANTS.toException();
        }

        Set<Long> memberIds = new LinkedHashSet<>();
        memberIds.add(command.requesterId());
        if (command.participantMemberIds() != null) {
            memberIds.addAll(command.participantMemberIds());
        }
        if (memberIds.size() < 3) {
            throw ChatErrorCode.INVALID_GROUP_PARTICIPANTS.toException();
        }

        ChatRoomJpaEntity room = chatRoomJpaRepository.save(ChatRoomJpaEntity.createGroup(command.title(), command.coverImageKey()));
        chatParticipantJpaRepository.save(ChatParticipantJpaEntity.active(room.getId(), command.requesterId(), ParticipantRole.OWNER));
        memberIds.stream()
                .filter(memberId -> !memberId.equals(command.requesterId()))
                .map(memberId -> ChatParticipantJpaEntity.active(room.getId(), memberId, ParticipantRole.MEMBER))
                .forEach(chatParticipantJpaRepository::save);
        return new CreateChatRoomResponse(room.getId(), RoomType.GROUP);
    }
}
