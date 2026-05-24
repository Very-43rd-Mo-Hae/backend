package com.very.relink.auth.adapter.out.social.apple;

import com.very.relink.auth.application.command.SocialLoginCommand;
import com.very.relink.auth.adapter.out.social.support.AbstractTemporarySocialUserResolver;
import com.very.relink.auth.domain.value.OAuth2Provider;
import org.springframework.stereotype.Component;

@Component
public class TemporaryAppleSocialUserResolver extends AbstractTemporarySocialUserResolver {

    @Override
    public OAuth2Provider supports() {
        return OAuth2Provider.APPLE;
    }

    @Override
    protected String resolveToken(SocialLoginCommand socialLoginCommand) {
        return socialLoginCommand.idToken();
    }
}
