package com.very.relink.friend.presentation.swagger;

import com.very.relink.core.configuration.swagger.ApiErrorCode;
import com.very.relink.core.presentation.RestResponse;
import com.very.relink.friend.application.response.FriendResponses.FriendListResponse;
import com.very.relink.friend.application.response.FriendResponses.FriendStatusListResponse;
import com.very.relink.friend.application.response.FriendResponses.RecommendedFriendListResponse;
import com.very.relink.friend.exception.FriendErrorCode;
import com.very.relink.friend.presentation.request.FriendLightningRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Friend", description = "친구 목록과 친구 상태 조회 API")
public interface FriendSwagger {

    @Operation(
            summary = "친구 목록 조회",
            description = "현재 로그인한 회원의 수락된 친구 목록을 페이지 단위로 조회합니다. 검색어를 전달하면 친구 이름 기준으로 필터링합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "친구 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = FriendListResponse.class))
    )
    @ApiErrorCode({FriendErrorCode.class})
    ResponseEntity<RestResponse<FriendListResponse>> getFriends(
            @Parameter(description = "친구 이름 검색어", example = "민수")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "0부터 시작하는 페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기. 최대 50명까지 조회할 수 있습니다.", example = "10")
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(
            summary = "추천 친구 조회",
            description = "친구 목록 화면 상단의 추천 영역에 노출할 친구를 조회합니다. 현재는 수락된 친구 중 이름순으로 limit명까지 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "추천 친구 조회 성공",
            content = @Content(schema = @Schema(implementation = RecommendedFriendListResponse.class))
    )
    @ApiErrorCode({FriendErrorCode.class})
    ResponseEntity<RestResponse<RecommendedFriendListResponse>> getRecommendedFriends(
            @Parameter(description = "추천 영역에 표시할 최대 친구 수", example = "10")
            @RequestParam(defaultValue = "10") int limit
    );

    @Operation(
            summary = "친구 상태 링 조회",
            description = "최대 10명의 친구에 대해 현재 시각부터 4시간 동안의 일정 상태와 번개 활성 여부를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "친구 상태 링 조회 성공",
            content = @Content(schema = @Schema(implementation = FriendStatusListResponse.class))
    )
    @ApiErrorCode({FriendErrorCode.class})
    ResponseEntity<RestResponse<FriendStatusListResponse>> getFriendStatuses(
            @Parameter(description = "상태를 조회할 친구 memberId 목록. 최대 10개까지 허용합니다.", example = "1")
            @RequestParam List<Long> memberIds
    );

    @Operation(
            summary = "번개 상태 설정",
            description = "현재 로그인한 회원을 요청한 종료 시각까지 번개 가능 상태로 설정합니다. Redis TTL은 expiresAt까지 남은 시간으로 저장됩니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "번개 상태 설정 성공"
    )
    @ApiErrorCode({FriendErrorCode.class})
    ResponseEntity<RestResponse<Void>> activateLightning(
            @RequestBody FriendLightningRequest request
    );
}
