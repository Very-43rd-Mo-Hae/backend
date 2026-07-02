package com.very.relink.chat.application.service;

import com.very.relink.chat.application.response.ChatResponses.ChatRoomParticipantResponse;
import com.very.relink.chat.application.response.ChatResponses.ChatRoomParticipantsResponse;
import com.very.relink.chat.domain.ChatEnums.ParticipantStatus;
import com.very.relink.chat.exception.ChatErrorCode;
import com.very.relink.chat.infrastructure.persistence.jpa.ChatParticipantJpaRepository;
import com.very.relink.member.adapter.out.persistence.MemberJpaEntity;
import com.very.relink.member.adapter.out.persistence.MemberJpaRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetChatRoomParticipantsQueryService {

    private final ChatParticipantJpaRepository chatParticipantJpaRepository;
    private final MemberJpaRepository memberJpaRepository;

    @Transactional(readOnly = true)
    public ChatRoomParticipantsResponse getParticipants(Long requesterId, Long roomId) {
        ChatValidationSupport.requireActiveParticipant(chatParticipantJpaRepository, roomId, requesterId);

        List<Long> participantMemberIds = chatParticipantJpaRepository
                .findByRoomIdAndStatus(roomId, ParticipantStatus.ACTIVE)
                .stream()
                .map(participant -> participant.getMemberId())
                .toList();

        if (participantMemberIds.isEmpty()) {
            throw ChatErrorCode.CHAT_PARTICIPANT_NOT_FOUND.toException();
        }

        Map<Long, MemberJpaEntity> membersById = new LinkedHashMap<>();
        memberJpaRepository.findAllById(participantMemberIds)
                .forEach(member -> membersById.put(member.getId(), member));

        return new ChatRoomParticipantsResponse(participantMemberIds.stream()
                .map(membersById::get)
                .filter(member -> member != null)
                .map(member -> new ChatRoomParticipantResponse(
                        member.getId(),
                        member.getName(),
                        member.getImageUrl()
                ))
                .toList());
    }
}
