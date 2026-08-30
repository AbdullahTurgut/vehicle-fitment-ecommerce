package com.carmats.payment.provider.dto;

import java.math.BigDecimal;

public record ProviderRefundResult(
        boolean success,
        String refundIdExternal,
        BigDecimal refundedAmount,
        String errorCode,
        String errorMessage,
        String rawRequest,
        String rawResponse
) {
    public static ProviderRefundResult success(
            String refundIdExternal,
            BigDecimal refundedAmount,
            String rawRequest,
            String rawResponse
    ) {
        return new ProviderRefundResult(true, refundIdExternal, refundedAmount, null, null, rawRequest, rawResponse);
    }

    public static ProviderRefundResult failure(
            String errorCode,
            String errorMessage,
            String rawRequest,
            String rawResponse
    ) {
        return new ProviderRefundResult(false, null, null, errorCode, errorMessage, rawRequest, rawResponse);
    }
}
