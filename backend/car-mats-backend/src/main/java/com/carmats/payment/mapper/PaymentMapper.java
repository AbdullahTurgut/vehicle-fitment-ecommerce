package com.carmats.payment.mapper;

import com.carmats.payment.dto.response.PaymentResponse;
import com.carmats.payment.dto.response.PaymentTransactionResponse;
import com.carmats.payment.entity.Payment;
import com.carmats.payment.entity.PaymentTransaction;

import java.util.Collections;
import java.util.List;

public final class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentResponse toResponse(Payment payment) {
        if (payment == null) {
            return null;
        }

        List<PaymentTransactionResponse> transactions = payment.getTransactions() != null
                ? payment.getTransactions().stream().map(PaymentMapper::toTransactionResponse).toList()
                : Collections.emptyList();

        return new PaymentResponse(
                payment.getId(),
                payment.getOrder() != null ? payment.getOrder().getId() : null,
                payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null,
                payment.getPaymentMethod(),
                payment.getPaymentProvider(),
                payment.getStatus(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getInstallment(),
                payment.getCardBin(),
                payment.getCardLastFour(),
                payment.getCardType(),
                payment.getCardFamily(),
                payment.getErrorCode(),
                payment.getErrorMessage(),
                payment.getPaidAt(),
                transactions,
                payment.getCreatedAt()
        );
    }

    public static PaymentTransactionResponse toTransactionResponse(PaymentTransaction transaction) {
        if (transaction == null) {
            return null;
        }

        return new PaymentTransactionResponse(
                transaction.getId(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getAmount(),
                transaction.getTransactionIdExternal(),
                transaction.getCreatedAt()
        );
    }
}
