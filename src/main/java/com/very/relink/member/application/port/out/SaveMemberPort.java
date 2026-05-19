package com.very.relink.member.application.port.out;

import com.very.relink.member.domain.Member;

public interface SaveMemberPort {

    Member save(Member member);
}
