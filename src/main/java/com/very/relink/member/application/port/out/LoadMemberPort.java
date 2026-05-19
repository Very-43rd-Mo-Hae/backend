package com.very.relink.member.application.port.out;

import com.very.relink.member.domain.Member;
import java.util.Optional;

public interface LoadMemberPort {

    Optional<Member> findByEmail(String email);
}
