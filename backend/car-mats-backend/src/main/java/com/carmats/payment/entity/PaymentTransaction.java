package com.carmats.payment.entity;

import com.carmats.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private PaymentTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_id_external", length = 100)
    private String transactionIdExternal;

    @Column(name = "raw_request", columnDefinition = "TEXT")
    private String rawRequest;

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    protected PaymentTransaction() {
    }

    public PaymentTransaction(
            Payment payment,
            PaymentTransactionType transactionType,
            PaymentStatus status,
            BigDecimal amount,
            String transactionIdExternal,
            String rawRequest,
            String rawResponse
    ) {
        this.payment = payment;
        this.transactionType = transactionType;
        this.status = status;
        this.amount = amount;
        this.transactionIdExternal = transactionIdExternal;
        this.rawRequest = rawRequest;
        this.rawResponse = rawResponse;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public PaymentTransactionType getTransactionType() {
        return transactionType;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTransactionIdExternal() {
        return transactionIdExternal;
    }

    public String getRawRequest() {
        return rawRequest;
    }

    public String getRawResponse() {
        return rawResponse;
    }
}
