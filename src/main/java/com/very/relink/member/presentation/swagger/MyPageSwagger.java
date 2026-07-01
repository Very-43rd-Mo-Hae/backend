package com.very.relink.member.presentation.swagger;

import com.very.relink.core.configuration.swagger.ApiErrorCode;
import com.very.relink.core.presentation.RestResponse;
import com.very.relink.member.application.response.MyPageResponse;
import com.very.relink.member.application.response.ProfileImageUploadResponse;
import com.very.relink.member.exception.MemberErrorCode;
import com.very.relink.member.presentation.controller.MyPageController.IssueProfileImageUploadRequest;
import com.very.relink.member.presentation.request.UpdateMyProfileRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "My Page", description = "내 프로필과 회원 상태 관리 API")
public interface MyPageSwagger {

    @Operation(
            summary = "내 마이페이지 조회",
            description = "현재 로그인한 회원의 프로필, 친구 수, 가입 provider, 계정 상태 정보를 조회합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "내 마이페이지 조회 성공",
            content = @Content(schema = @Schema(implementation = MyPageResponse.class))
    )
    @ApiErrorCode({MemberErrorCode.class})
    ResponseEntity<RestResponse<MyPageResponse>> getMyPage();

    @Operation(
            summary = "프로필 이미지 업로드 URL 발급",
            description = "프로필 이미지를 업로드할 presigned URL과 업로드 완료 후 저장할 이미지 URL을 발급합니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "프로필 이미지 업로드 URL 발급 성공",
            content = @Content(schema = @Schema(implementation = ProfileImageUploadResponse.class))
    )
    @ApiErrorCode({MemberErrorCode.class})
    ResponseEntity<RestResponse<ProfileImageUploadResponse>> issueProfileImageUploadUrl(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "업로드할 프로필 이미지 파일명, 콘텐츠 타입, 파일 크기",
                    required = true
            )
            @RequestBody IssueProfileImageUploadRequest request
    );

    @Operation(
            summary = "내 프로필 수정",
            description = "현재 로그인한 회원의 닉네임, 상태 메시지, 프로필 이미지 URL을 수정합니다. 전달하지 않은 값은 서비스 정책에 따라 기존 값이 유지되거나 비워질 수 있습니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "내 프로필 수정 성공",
            content = @Content(schema = @Schema(implementation = MyPageResponse.class))
    )
    @ApiErrorCode({MemberErrorCode.class})
    ResponseEntity<RestResponse<MyPageResponse>> updateProfile(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 프로필 정보",
                    required = true
            )
            @RequestBody UpdateMyProfileRequest request
    );

    @Operation(
            summary = "회원 탈퇴",
            description = "현재 로그인한 회원을 탈퇴 처리합니다. 탈퇴 후 회원 상태가 비활성으로 변경됩니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "회원 탈퇴 성공",
            content = @Content(schema = @Schema(implementation = RestResponse.class))
    )
    @ApiErrorCode({MemberErrorCode.class})
    ResponseEntity<RestResponse<Void>> withdraw();
}
