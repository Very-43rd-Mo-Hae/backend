package com.very.relink.core.presentation;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseResponse {

    private Boolean isSuccess;

    private final LocalDateTime timestamp;
}
