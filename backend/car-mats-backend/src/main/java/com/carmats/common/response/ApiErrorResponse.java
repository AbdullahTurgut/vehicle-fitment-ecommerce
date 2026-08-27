package com.carmats.common.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiErrorResponse(

        int status,

        String code,

        String message,

        Map<String, String> errors,

        LocalDateTime timestamp

) {
}