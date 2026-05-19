package com.very.relink.auth.application.port.out;

import com.very.relink.auth.domain.token.AuthTokens;
import com.very.relink.member.domain.Member;

public interface TokenIssuePort {

    AuthTokens issue(Member member);
}
