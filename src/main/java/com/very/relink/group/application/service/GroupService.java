package com.very.relink.group.application.service;

import com.very.relink.group.adapter.out.persistence.MemberGroupParticipantJpaRepository;
import com.very.relink.group.application.response.GroupResponses.GroupListResponse;
import com.very.relink.group.application.response.GroupResponses.GroupSummaryResponse;
import com.very.relink.group.domain.MemberGroupParticipantStatus;
import com.very.relink.member.adapter.out.persistence.MemberJpaRepository;
import com.very.relink.member.exception.MemberErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GroupService {

    private static final int MAX_GROUP_LIMIT = 50;

    private final MemberJpaRepository memberJpaRepository;
    private final MemberGroupParticipantJpaRepository memberGroupParticipantJpaRepository;

    @Transactional(readOnly = true)
    public GroupListResponse getMyGroups(Long memberId, int limit) {
        validateMemberExists(memberId);
        if (limit < 1 || limit > MAX_GROUP_LIMIT) {
            limit = MAX_GROUP_LIMIT;
        }

        return new GroupListResponse(
                memberGroupParticipantJpaRepository
                        .findActiveGroupSummariesByMemberId(
                                memberId,
                                MemberGroupParticipantStatus.ACTIVE,
                                PageRequest.of(0, limit)
                        )
                        .stream()
                        .map(group -> new GroupSummaryResponse(
                                group.getGroupId(),
                                group.getName(),
                                group.getMemberCount()
                        ))
                        .toList()
        );
    }

    private void validateMemberExists(Long memberId) {
        if (!memberJpaRepository.existsById(memberId)) {
            throw MemberErrorCode.MEMBER_NOT_FOUND.toException();
        }
    }
}
