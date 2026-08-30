package com.carmats.payment.provider.dto;

import java.math.BigDecimal;

public record ProviderPaymentResult(
        boolean success,
        String paymentIdExternal,
        String conversationId,
        BigDecimal paidAmount,
        String cardBin,
        String cardLastFour,
        String cardType,
        String cardFamily,
        String errorCode,
        String errorMessage,
        String rawRequest,
        String rawResponse
) {
    public static ProviderPaymentResult success(
            String paymentIdExternal,
            String conversationId,
            BigDecimal paidAmount,
            String cardBin,
            String cardLastFour,
            String cardType,
            String cardFamily,
            String rawRequest,
            String rawResponse
    ) {
        return new ProviderPaymentResult(
                true,
                paymentIdExternal,
                conversationId,
                paidAmount,
                cardBin,
                cardLastFour,
                cardType,
                cardFamily,
                null,
                null,
                rawRequest,
                rawResponse
        );
    }

    public static ProviderPaymentResult failure(
            String errorCode,
            String errorMessage,
            String rawRequest,
            String rawResponse
    ) {
        return new ProviderPaymentResult(
                false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                errorCode,
                errorMessage,
                rawRequest,
                rawResponse
        );
    }
}
