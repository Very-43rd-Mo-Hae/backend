package com.very.relink.member.application.service;

import com.very.relink.auth.application.service.TokenService;
import com.very.relink.chat.application.command.ChatCommands.IssueProfileImagePresignedUrlCommand;
import com.very.relink.chat.application.port.ChatPorts.StoragePresignedUrlPort;
import com.very.relink.chat.application.port.ChatPorts.StorageUrlResolver;
import com.very.relink.chat.application.response.ChatResponses.PresignedUploadUrl;
import com.very.relink.chat.application.service.ChatValidationSupport;
import com.very.relink.friend.adapter.out.persistence.FriendshipJpaRepository;
import com.very.relink.friend.domain.FriendshipStatus;
import com.very.relink.member.application.port.out.LoadMemberPort;
import com.very.relink.member.application.port.out.SaveMemberPort;
import com.very.relink.member.application.response.MyPageResponse;
import com.very.relink.member.application.response.ProfileImageUploadResponse;
import com.very.relink.member.domain.Member;
import com.very.relink.member.exception.MemberErrorCode;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final FriendshipJpaRepository friendshipJpaRepository;
    private final TokenService tokenService;
    private final StoragePresignedUrlPort storagePresignedUrlPort;
    private final StorageUrlResolver storageUrlResolver;

    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(Long memberId) {
        Member member = loadMember(memberId);
        long friendCount = friendshipJpaRepository.countFriends(memberId, FriendshipStatus.ACCEPTED, null);

        return MyPageResponse.of(member, friendCount);
    }

    @Transactional
    public MyPageResponse updateProfile(Long memberId, String name, String bio, String imageUrl) {
        Member member = loadMember(memberId);
        member.updateProfile(
                normalizeName(name, member.getName()),
                normalizeText(bio),
                normalizeText(imageUrl)
        );

        Member savedMember = saveMemberPort.save(member);
        long friendCount = friendshipJpaRepository.countFriends(memberId, FriendshipStatus.ACCEPTED, null);
        return MyPageResponse.of(savedMember, friendCount);
    }

    public ProfileImageUploadResponse issueProfileImageUploadUrl(
            Long memberId,
            String fileName,
            String contentType,
            Long fileSize
    ) {
        loadMember(memberId);
        ChatValidationSupport.validateImageFile(contentType, fileSize);

        PresignedUploadUrl upload = storagePresignedUrlPort.issueProfileImageUploadUrl(
                new IssueProfileImagePresignedUrlCommand(memberId, fileName, contentType, fileSize)
        );

        return new ProfileImageUploadResponse(
                upload.uploadUrl(),
                upload.storageKey(),
                storageUrlResolver.resolveUrl(upload.storageKey()),
                upload.expiresIn()
        );
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = loadMember(memberId);
        if (member.isWithdrawn()) {
            throw MemberErrorCode.MEMBER_ALREADY_WITHDRAWN.toException();
        }

        member.withdraw(LocalDateTime.now());
        saveMemberPort.save(member);
        tokenService.logoutAll(memberId);
    }

    private Member loadMember(Long memberId) {
        return loadMemberPort.findById(memberId)
                .orElseThrow(MemberErrorCode.MEMBER_NOT_FOUND::toException);
    }

    private String normalizeName(String value, String fallback) {
        String normalized = normalizeText(value);
        return normalized == null ? fallback : normalized;
    }

    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
