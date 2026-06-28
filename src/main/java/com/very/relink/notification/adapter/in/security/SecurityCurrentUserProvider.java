package com.very.relink.notification.adapter.in.security;

import com.very.relink.auth.adapter.in.security.CustomUserDetail;
import com.very.relink.auth.exception.AuthErrorCode;
import com.very.relink.notification.application.port.in.CurrentUserProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentUserProvider implements CurrentUserProvider {

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetail userDetail)) {
            throw AuthErrorCode.AUTHENTICATION_REQUIRED.toException();
        }

        return userDetail.getMemberId();
    }
}
