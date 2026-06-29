package com.very.relink.friend.application.service;

import com.very.relink.friend.adapter.out.persistence.FriendshipJpaEntity;
import com.very.relink.friend.adapter.out.persistence.FriendshipJpaRepository;
import com.very.relink.friend.adapter.out.redis.FriendLightningRedisAdapter;
import com.very.relink.friend.application.response.FriendResponses.FriendListResponse;
import com.very.relink.friend.application.response.FriendResponses.FriendStatusListResponse;
import com.very.relink.friend.application.response.FriendResponses.FriendStatusResponse;
import com.very.relink.friend.application.response.FriendResponses.FriendStatusSlotResponse;
import com.very.relink.friend.application.response.FriendResponses.FriendSummaryResponse;
import com.very.relink.friend.application.response.FriendResponses.RecommendedFriendListResponse;
import com.very.relink.friend.domain.FriendshipStatus;
import com.very.relink.friend.exception.FriendErrorCode;
import com.very.relink.member.adapter.out.persistence.MemberJpaEntity;
import com.very.relink.member.adapter.out.persistence.MemberJpaRepository;
import com.very.relink.schedule.application.response.ScheduleResponses.UpcomingScheduleStatusResponse;
import com.very.relink.schedule.application.service.ScheduleService;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_STATUS_MEMBER_IDS = 10;

    private final MemberJpaRepository memberJpaRepository;
    private final FriendshipJpaRepository friendshipJpaRepository;
    private final ScheduleService scheduleService;
    private final FriendLightningRedisAdapter friendLightningRedisAdapter;

    @Transactional(readOnly = true)
    public FriendListResponse getFriends(Long memberId, String keyword, int page, int size) {
        validateMemberExists(memberId);
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw FriendErrorCode.INVALID_PAGE_REQUEST.toException();
        }

        String normalizedKeyword = normalizeKeyword(keyword);
        Pageable pageable = PageRequest.of(page, size);
        long totalCount = friendshipJpaRepository.countFriends(memberId, FriendshipStatus.ACCEPTED, normalizedKeyword);
        List<FriendSummaryResponse> friends = friendshipJpaRepository
                .findFriends(memberId, FriendshipStatus.ACCEPTED, normalizedKeyword, pageable)
                .stream()
                .map(friendship -> toFriendSummary(memberId, friendship))
                .toList();

        return new FriendListResponse(
                totalCount,
                page,
                size,
                ((long) (page + 1) * size) < totalCount,
                friends
        );
    }

    @Transactional(readOnly = true)
    public RecommendedFriendListResponse getRecommendedFriends(Long memberId, int limit) {
        validateMemberExists(memberId);
        if (limit < 1 || limit > MAX_PAGE_SIZE) {
            throw FriendErrorCode.INVALID_PAGE_REQUEST.toException();
        }

        List<FriendSummaryResponse> friends = friendshipJpaRepository
                .findFriends(memberId, FriendshipStatus.ACCEPTED, null, PageRequest.of(0, limit))
                .stream()
                .map(friendship -> toFriendSummary(memberId, friendship))
                .toList();

        return new RecommendedFriendListResponse(friends);
    }

    @Transactional(readOnly = true)
    public FriendStatusListResponse getFriendStatuses(Long currentMemberId, List<Long> memberIds) {
        friendLightningRedisAdapter.refreshActive(currentMemberId);

        List<Long> requestedMemberIds = normalizeStatusMemberIds(memberIds);
        UpcomingScheduleStatusResponse scheduleStatus = scheduleService.getUpcomingStatuses(requestedMemberIds);
        Map<Long, Boolean> activeMap = friendLightningRedisAdapter.getActiveMap(requestedMemberIds);

        return new FriendStatusListResponse(
                scheduleStatus.from(),
                scheduleStatus.to(),
                scheduleStatus.members().stream()
                        .map(member -> new FriendStatusResponse(
                                member.memberId(),
                                activeMap.getOrDefault(member.memberId(), false),
                                member.slots().stream()
                                        .map(slot -> new FriendStatusSlotResponse(
                                                slot.date(),
                                                slot.startTime(),
                                                slot.endTime(),
                                                slot.status(),
                                                slot.appointmentId()
                                        ))
                                        .toList()
                        ))
                        .toList()
        );
    }

    private void validateMemberExists(Long memberId) {
        if (!memberJpaRepository.existsById(memberId)) {
            throw FriendErrorCode.MEMBER_NOT_FOUND.toException();
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }

    private List<Long> normalizeStatusMemberIds(List<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty() || memberIds.size() > MAX_STATUS_MEMBER_IDS) {
            throw FriendErrorCode.INVALID_STATUS_MEMBER_IDS.toException();
        }

        Set<Long> uniqueMemberIds = new LinkedHashSet<>();
        for (Long memberId : memberIds) {
            if (memberId == null) {
                throw FriendErrorCode.INVALID_STATUS_MEMBER_IDS.toException();
            }
            uniqueMemberIds.add(memberId);
        }
        if (uniqueMemberIds.size() > MAX_STATUS_MEMBER_IDS) {
            throw FriendErrorCode.INVALID_STATUS_MEMBER_IDS.toException();
        }
        return List.copyOf(uniqueMemberIds);
    }

    private FriendSummaryResponse toFriendSummary(Long memberId, FriendshipJpaEntity friendship) {
        MemberJpaEntity friend = friendship.getMemberLow().getId().equals(memberId)
                ? friendship.getMemberHigh()
                : friendship.getMemberLow();

        return new FriendSummaryResponse(friend.getId(), friend.getName(), friend.getImageUrl());
    }
}
