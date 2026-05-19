package com.very.relink.member.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Member {

    private final Long id;
    private String email;
    private String name;
    private String imageUrl;

    public static Member create(String email, String name, String imageUrl) {
        return Member.builder()
                .email(email)
                .name(name)
                .imageUrl(imageUrl)
                .build();
    }

    public void updateProfile(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }
}
