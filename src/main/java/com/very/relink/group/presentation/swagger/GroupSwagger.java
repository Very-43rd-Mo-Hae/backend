package com.very.relink.group.presentation.swagger;

import com.very.relink.core.presentation.RestResponse;
import com.very.relink.group.application.response.GroupResponses.GroupListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Group", description = "회원 그룹 조회 API")
public interface GroupSwagger {

    @Operation(
            summary = "내 그룹 목록 조회",
            description = "현재 로그인한 회원이 ACTIVE 상태로 속한 그룹 목록을 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "그룹 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = GroupListResponse.class))
    )
    ResponseEntity<RestResponse<GroupListResponse>> getMyGroups(
            @Parameter(description = "표시할 최대 그룹 수. 최대 50개까지 조회합니다.", example = "10")
            @RequestParam(defaultValue = "10") int limit
    );
}
