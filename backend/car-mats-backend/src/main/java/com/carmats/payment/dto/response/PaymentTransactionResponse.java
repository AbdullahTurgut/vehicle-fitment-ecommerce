package com.carmats.payment.dto.response;

import com.carmats.payment.entity.PaymentStatus;
import com.carmats.payment.entity.PaymentTransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentTransactionResponse(
        UUID id,
        PaymentTransactionType transactionType,
        PaymentStatus status,
        BigDecimal amount,
        String transactionIdExternal,
        LocalDateTime createdAt
) {
}
