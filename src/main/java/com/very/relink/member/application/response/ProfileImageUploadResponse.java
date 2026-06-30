package com.very.relink.member.application.response;

public record ProfileImageUploadResponse(
        String uploadUrl,
        String storageKey,
        String imageUrl,
        long expiresIn
) {
}
