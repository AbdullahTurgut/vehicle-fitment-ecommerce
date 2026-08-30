package com.carmats.payment.dto.response;

import com.carmats.payment.entity.PaymentMethod;
import com.carmats.payment.entity.PaymentProviderType;
import com.carmats.payment.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        String orderNumber,
        PaymentMethod paymentMethod,
        PaymentProviderType paymentProvider,
        PaymentStatus status,
        BigDecimal amount,
        String currency,
        int installment,
        String cardBin,
        String cardLastFour,
        String cardType,
        String cardFamily,
        String errorCode,
        String errorMessage,
        LocalDateTime paidAt,
        List<PaymentTransactionResponse> transactions,
        LocalDateTime createdAt
) {
}
