package com.very.relink.auth.application.port.in;

import com.very.relink.auth.application.result.OAuth2LoginResult;
import com.very.relink.auth.application.command.OAuth2LoginCommand;

public interface OAuth2LoginUseCase {

    OAuth2LoginResult login(OAuth2LoginCommand oAuth2LoginCommand);
}
