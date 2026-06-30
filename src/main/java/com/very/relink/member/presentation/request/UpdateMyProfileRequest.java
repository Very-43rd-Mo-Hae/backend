package com.very.relink.member.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateMyProfileRequest(
        @Schema(description = "닉네임", example = "윤창현")
        String name,
        @Schema(description = "상태 메시지", example = "오늘 저녁 가능")
        String bio,
        @Schema(description = "업로드 완료 후 저장할 프로필 이미지 공개 URL", example = "https://example.com/profile.png")
        String imageUrl
) {
}
