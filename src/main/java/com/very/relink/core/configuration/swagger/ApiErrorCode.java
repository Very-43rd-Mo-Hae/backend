package com.very.relink.core.configuration.swagger;

import com.very.relink.core.exception.error.BaseErrorCode;

public @interface ApiErrorCode {
    Class<? extends BaseErrorCode>[] value();
}
