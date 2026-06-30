package com.very.relink.friend.presentation.controller;

import com.very.relink.core.presentation.RestResponse;
import com.very.relink.friend.application.response.FriendResponses.FriendListResponse;
import com.very.relink.friend.application.response.FriendResponses.FriendStatusListResponse;
import com.very.relink.friend.application.response.FriendResponses.RecommendedFriendListResponse;
import com.very.relink.friend.application.service.FriendService;
import com.very.relink.friend.presentation.request.FriendLightningRequest;
import com.very.relink.friend.presentation.swagger.FriendSwagger;
import com.very.relink.notification.application.port.in.CurrentUserProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/friends")
public class FriendController implements FriendSwagger {

    private final CurrentUserProvider currentUserProvider;
    private final FriendService friendService;

    @GetMapping
    @Override
    public ResponseEntity<RestResponse<FriendListResponse>> getFriends(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(
                friendService.getFriends(memberId, keyword, page, size)
        ));
    }

    @GetMapping("/recommendations")
    @Override
    public ResponseEntity<RestResponse<RecommendedFriendListResponse>> getRecommendedFriends(
            @RequestParam(defaultValue = "10") int limit
    ) {
        Long memberId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(
                friendService.getRecommendedFriends(memberId, limit)
        ));
    }

    @GetMapping("/status")
    @Override
    public ResponseEntity<RestResponse<FriendStatusListResponse>> getFriendStatuses(
            @RequestParam List<Long> memberIds
    ) {
        Long memberId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(
                friendService.getFriendStatuses(memberId, memberIds)
        ));
    }

    @PostMapping("/lightning")
    @Override
    public ResponseEntity<RestResponse<Void>> activateLightning(
            @RequestBody FriendLightningRequest request
    ) {
        Long memberId = currentUserProvider.getCurrentUserId();
        friendService.activateLightning(memberId, request == null ? null : request.expiresAt());
        return ResponseEntity.ok(new RestResponse<>(null));
    }
}
