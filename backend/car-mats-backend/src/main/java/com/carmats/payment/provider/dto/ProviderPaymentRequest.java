package com.carmats.payment.provider.dto;

import java.math.BigDecimal;

public record ProviderPaymentRequest(
        String orderNumber,
        BigDecimal amount,
        String currency,
        String cardHolderName,
        String cardNumber,
        String expireMonth,
        String expireYear,
        String cvc,
        int installment
) {
}
