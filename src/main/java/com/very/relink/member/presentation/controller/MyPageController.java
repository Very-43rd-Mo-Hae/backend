package com.very.relink.member.presentation.controller;

import com.very.relink.core.presentation.RestResponse;
import com.very.relink.member.application.response.MyPageResponse;
import com.very.relink.member.application.response.ProfileImageUploadResponse;
import com.very.relink.member.application.service.MyPageService;
import com.very.relink.member.presentation.request.UpdateMyProfileRequest;
import com.very.relink.notification.application.port.in.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me")
public class MyPageController {

    private final CurrentUserProvider currentUserProvider;
    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<RestResponse<MyPageResponse>> getMyPage() {
        Long memberId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(myPageService.getMyPage(memberId)));
    }

    @PostMapping("/profile-image/presigned-url")
    public ResponseEntity<RestResponse<ProfileImageUploadResponse>> issueProfileImageUploadUrl(
            @RequestBody IssueProfileImageUploadRequest request
    ) {
        Long memberId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(myPageService.issueProfileImageUploadUrl(
                memberId,
                request == null ? null : request.fileName(),
                request == null ? null : request.contentType(),
                request == null ? null : request.fileSize()
        )));
    }

    @PatchMapping("/profile")
    public ResponseEntity<RestResponse<MyPageResponse>> updateProfile(
            @RequestBody UpdateMyProfileRequest request
    ) {
        Long memberId = currentUserProvider.getCurrentUserId();
        return ResponseEntity.ok(new RestResponse<>(myPageService.updateProfile(
                memberId,
                request == null ? null : request.name(),
                request == null ? null : request.bio(),
                request == null ? null : request.imageUrl()
        )));
    }

    @DeleteMapping
    public ResponseEntity<RestResponse<Void>> withdraw() {
        Long memberId = currentUserProvider.getCurrentUserId();
        myPageService.withdraw(memberId);
        return ResponseEntity.ok(new RestResponse<>(null));
    }

    public record IssueProfileImageUploadRequest(
            String fileName,
            String contentType,
            Long fileSize
    ) {
    }
}
