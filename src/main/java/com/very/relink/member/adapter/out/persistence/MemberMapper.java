package com.very.relink.member.adapter.out.persistence;

import com.very.relink.member.domain.Member;
import com.very.relink.member.domain.MemberStatus;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public Member toDomain(MemberJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Member.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .name(entity.getName())
                .bio(entity.getBio())
                .imageUrl(entity.getImageUrl())
                .provider(entity.getProvider())
                .providerId(entity.getProviderId())
                .status(entity.getStatus() == null ? MemberStatus.ACTIVE : entity.getStatus())
                .withdrawnAt(entity.getWithdrawnAt())
                .build();
    }

    public MemberJpaEntity toEntity(Member member) {
        if (member == null) {
            return null;
        }

        return MemberJpaEntity.builder()
                .id(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .bio(member.getBio())
                .imageUrl(member.getImageUrl())
                .provider(member.getProvider())
                .providerId(member.getProviderId())
                .status(member.getStatus())
                .withdrawnAt(member.getWithdrawnAt())
                .build();
    }
}
