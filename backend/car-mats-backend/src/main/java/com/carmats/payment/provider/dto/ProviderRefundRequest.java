package com.carmats.payment.provider.dto;

import java.math.BigDecimal;

public record ProviderRefundRequest(
        String paymentIdExternal,
        BigDecimal amount,
        String currency,
        String reason
) {
}
