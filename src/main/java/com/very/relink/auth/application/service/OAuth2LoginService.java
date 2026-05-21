package com.very.relink.auth.application.service;

import com.very.relink.auth.application.port.in.OAuth2LoginUseCase;
import com.very.relink.auth.application.port.out.TokenIssuePort;
import com.very.relink.auth.application.result.OAuth2LoginResult;
import com.very.relink.auth.application.command.OAuth2LoginCommand;
import com.very.relink.auth.domain.token.AuthTokens;
import com.very.relink.auth.exception.AuthErrorCode;
import com.very.relink.member.application.port.out.LoadMemberPort;
import com.very.relink.member.application.port.out.SaveMemberPort;
import com.very.relink.member.domain.Member;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuth2LoginService implements OAuth2LoginUseCase {

    private final LoadMemberPort loadMemberPort;
    private final SaveMemberPort saveMemberPort;
    private final TokenIssuePort tokenIssuePort;

    @Override
    @Transactional
    public OAuth2LoginResult login(OAuth2LoginCommand oAuth2LoginCommand) {
        String email = oAuth2LoginCommand.email();
        if(email == null || email.isEmpty()) {
            throw AuthErrorCode.OAUTH2_EMAIL_NOT_FOUND.toException();
        }

        Member member = loadMemberPort.findByEmail(email)
                .orElseGet(() -> saveMemberPort.save(
                        Member.create(
                                email,
                                oAuth2LoginCommand.name(),
                                oAuth2LoginCommand.imageUrl(),
                                oAuth2LoginCommand.provider()
                        )
                ));

        AuthTokens authTokens = tokenIssuePort.issue(member);
        return new OAuth2LoginResult(member.getId(), authTokens);
    }
}
