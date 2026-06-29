package com.very.relink.schedule.presentation.swagger;

import com.very.relink.core.configuration.swagger.ApiErrorCode;
import com.very.relink.core.presentation.RestResponse;
import com.very.relink.schedule.application.response.ScheduleResponses.UpdateScheduleSlotsResponse;
import com.very.relink.schedule.application.response.ScheduleResponses.UpcomingScheduleStatusResponse;
import com.very.relink.schedule.application.response.ScheduleResponses.WeeklyScheduleResponse;
import com.very.relink.schedule.exception.ScheduleErrorCode;
import com.very.relink.schedule.presentation.controller.ScheduleController.UpdateScheduleSlotsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Schedule", description = "회원 주간 일정 API")
public interface ScheduleSwagger {

    @Operation(
            summary = "주간 일정 조회",
            description = "지정한 날짜가 포함된 주의 일정 슬롯을 조회합니다. 각 슬롯은 사용 가능, 바쁨 등 일정 상태를 포함합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "주간 일정 조회 성공",
            content = @Content(schema = @Schema(implementation = WeeklyScheduleResponse.class))
    )
    @ApiErrorCode({ScheduleErrorCode.class})
    ResponseEntity<RestResponse<WeeklyScheduleResponse>> getWeeklySchedule(
            @Parameter(description = "조회 기준 날짜. 이 날짜가 포함된 주간 일정이 반환됩니다.", example = "2026-06-29")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );

    @Operation(
            summary = "현재 이후 4시간 일정 상태 조회",
            description = "여러 회원 id를 받아 현재 시간이 포함된 슬롯부터 앞으로 4시간의 일정 상태를 회원별로 반환합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "현재 이후 일정 상태 조회 성공",
            content = @Content(schema = @Schema(implementation = UpcomingScheduleStatusResponse.class))
    )
    @ApiErrorCode({ScheduleErrorCode.class})
    ResponseEntity<RestResponse<UpcomingScheduleStatusResponse>> getUpcomingStatuses(
            @Parameter(description = "조회할 회원 id 목록. memberIds=1&memberIds=2 형태로 전달합니다.", example = "1")
            @RequestParam List<Long> memberIds
    );

    @Operation(
            summary = "일정 슬롯 수정",
            description = "현재 로그인한 회원의 일정 슬롯 상태를 일괄 수정합니다. 슬롯 시간은 서비스 정책에 맞는 단위로 전달해야 합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "일정 슬롯 수정 성공",
            content = @Content(schema = @Schema(implementation = UpdateScheduleSlotsResponse.class))
    )
    @ApiErrorCode({ScheduleErrorCode.class})
    ResponseEntity<RestResponse<UpdateScheduleSlotsResponse>> updateSlots(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 일정 슬롯 목록",
                    required = true
            )
            @Valid @RequestBody UpdateScheduleSlotsRequest request
    );
}
