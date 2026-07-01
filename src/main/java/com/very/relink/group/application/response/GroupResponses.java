package com.very.relink.group.application.response;

import java.util.List;

public final class GroupResponses {

    private GroupResponses() {
    }

    public record GroupListResponse(
            List<GroupSummaryResponse> groups
    ) {
    }

    public record GroupSummaryResponse(
            Long groupId,
            String name,
            long memberCount
    ) {
    }
}
