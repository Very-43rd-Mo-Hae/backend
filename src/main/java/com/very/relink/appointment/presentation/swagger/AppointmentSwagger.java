package com.very.relink.appointment.presentation.swagger;

import com.very.relink.appointment.application.response.AppointmentResponses.AppointmentResponse;
import com.very.relink.appointment.application.response.AppointmentResponses.AvailableFriendListResponse;
import com.very.relink.appointment.application.response.AppointmentResponses.FriendCalendarListResponse;
import com.very.relink.appointment.application.response.AppointmentResponses.UpcomingAppointmentListResponse;
import com.very.relink.appointment.exception.AppointmentErrorCode;
import com.very.relink.appointment.presentation.controller.AppointmentController.CreateAppointmentRequest;
import com.very.relink.core.configuration.swagger.ApiErrorCode;
import com.very.relink.core.presentation.RestResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Appointment", description = "약속 생성과 친구 일정 확인 API")
public interface AppointmentSwagger {

    @Operation(
            summary = "다가오는 약속 목록 조회",
            description = "현재 로그인한 회원이 주최자이거나 참여자로 포함된 다가오는 약속을 시작 시각 오름차순으로 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "다가오는 약속 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = UpcomingAppointmentListResponse.class))
    )
    @ApiErrorCode({AppointmentErrorCode.class})
    ResponseEntity<RestResponse<UpcomingAppointmentListResponse>> getUpcomingAppointments(
            @Parameter(description = "표시할 최대 약속 수. 최대 20개까지 조회합니다.", example = "5")
            @RequestParam(defaultValue = "5") int limit
    );

    @Operation(
            summary = "약속 가능 친구 조회",
            description = "지정한 시작/종료 시간에 약속 참여가 가능한 친구 목록을 조회합니다. 각 친구의 주간 일정 정보도 함께 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "약속 가능 친구 조회 성공",
            content = @Content(schema = @Schema(implementation = AvailableFriendListResponse.class))
    )
    @ApiErrorCode({AppointmentErrorCode.class})
    ResponseEntity<RestResponse<AvailableFriendListResponse>> getAvailableFriends(
            @Parameter(description = "약속 시작 일시", example = "2026-07-01T19:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startAt,
            @Parameter(description = "약속 종료 일시", example = "2026-07-01T20:30:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endAt
    );

    @Operation(
            summary = "친구 캘린더 목록 조회",
            description = "선택한 친구들의 특정 날짜가 포함된 주간 일정 캘린더를 조회합니다. 약속 생성 화면에서 참여자별 가능 시간을 비교할 때 사용합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "친구 캘린더 목록 조회 성공",
            content = @Content(schema = @Schema(implementation = FriendCalendarListResponse.class))
    )
    @ApiErrorCode({AppointmentErrorCode.class})
    ResponseEntity<RestResponse<FriendCalendarListResponse>> getFriendCalendars(
            @Parameter(description = "캘린더를 조회할 친구 memberId 목록. memberIds=1&memberIds=2 형태로 전달합니다.", example = "1")
            @RequestParam List<Long> memberIds,
            @Parameter(description = "조회 기준 날짜. 해당 날짜가 포함된 주간 일정이 반환됩니다.", example = "2026-07-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );

    @Operation(
            summary = "약속 생성",
            description = "현재 로그인한 회원을 주최자로 하여 약속을 생성합니다. 참여자는 수락된 친구여야 하며, 시작/종료 시간은 서비스의 일정 단위에 맞아야 합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "약속 생성 성공",
            content = @Content(schema = @Schema(implementation = AppointmentResponse.class))
    )
    @ApiErrorCode({AppointmentErrorCode.class})
    ResponseEntity<RestResponse<AppointmentResponse>> createAppointment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "약속 제목, 시간, 메모, 참여자 memberId 목록",
                    required = true
            )
            @Valid @RequestBody CreateAppointmentRequest request
    );
}
