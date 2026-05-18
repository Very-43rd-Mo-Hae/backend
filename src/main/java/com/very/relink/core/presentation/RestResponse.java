package com.very.relink.core.presentation;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"success", "timestamp", "data"})
public class RestResponse<T> extends BaseResponse {
    T data;

    public RestResponse(T data) {
        super(true, LocalDateTime.now());
        this.data = data;
    }
}
