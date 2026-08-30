package com.carmats.payment.entity;

import com.carmats.common.entity.BaseEntity;
import com.carmats.order.entity.Order;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_provider", nullable = false, length = 50)
    private PaymentProviderType paymentProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 10)
    private String currency = "TRY";

    @Column(name = "conversation_id", length = 100)
    private String conversationId;

    @Column(name = "payment_id_external", length = 100)
    private String paymentIdExternal;

    @Column(nullable = false)
    private int installment = 1;

    @Column(name = "card_bin", length = 6)
    private String cardBin;

    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;

    @Column(name = "card_type", length = 30)
    private String cardType;

    @Column(name = "card_association", length = 30)
    private String cardAssociation;

    @Column(name = "card_family", length = 50)
    private String cardFamily;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentTransaction> transactions = new ArrayList<>();

    protected Payment() {
    }

    public Payment(
            Order order,
            PaymentMethod paymentMethod,
            PaymentProviderType paymentProvider,
            BigDecimal amount,
            String currency,
            int installment
    ) {
        this.order = order;
        this.paymentMethod = paymentMethod;
        this.paymentProvider = paymentProvider;
        this.amount = amount;
        this.currency = currency != null ? currency : "TRY";
        this.installment = installment > 0 ? installment : 1;
        this.status = PaymentStatus.PENDING;
    }

    public void addTransaction(PaymentTransaction transaction) {
        transactions.add(transaction);
        transaction.setPayment(this);
    }

    public void markSuccess(String externalId, String cardBin, String cardLastFour, String cardType, String cardFamily) {
        this.status = PaymentStatus.SUCCESS;
        this.paymentIdExternal = externalId;
        this.cardBin = cardBin;
        this.cardLastFour = cardLastFour;
        this.cardType = cardType;
        this.cardFamily = cardFamily;
        this.paidAt = LocalDateTime.now();
        this.errorCode = null;
        this.errorMessage = null;
    }

    public void markFailed(String errorCode, String errorMessage) {
        this.status = PaymentStatus.FAILED;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public void markRefunded() {
        this.status = PaymentStatus.REFUNDED;
    }

    public Order getOrder() {
        return order;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public PaymentProviderType getPaymentProvider() {
        return paymentProvider;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getPaymentIdExternal() {
        return paymentIdExternal;
    }

    public int getInstallment() {
        return installment;
    }

    public String getCardBin() {
        return cardBin;
    }

    public String getCardLastFour() {
        return cardLastFour;
    }

    public String getCardType() {
        return cardType;
    }

    public String getCardAssociation() {
        return cardAssociation;
    }

    public String getCardFamily() {
        return cardFamily;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public List<PaymentTransaction> getTransactions() {
        return transactions;
    }
}
