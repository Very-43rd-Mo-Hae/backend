package com.very.relink.group.presentation.controller;

import com.very.relink.core.presentation.RestResponse;
import com.very.relink.group.application.response.GroupResponses.GroupListResponse;
import com.very.relink.group.application.service.GroupService;
import com.very.relink.group.presentation.swagger.GroupSwagger;
import com.very.relink.notification.application.port.in.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupController implements GroupSwagger {

    private final CurrentUserProvider currentUserProvider;
    private final GroupService groupService;

    @GetMapping
    @Override
    public ResponseEntity<RestResponse<GroupListResponse>> getMyGroups(
            @RequestParam(defaultValue = "10") int limit
    ) {
        Long memberId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(groupService.getMyGroups(memberId, limit)));
    }
}
