package com.carmats.payment.service;

import com.carmats.common.exception.BusinessException;
import com.carmats.common.exception.NotFoundException;
import com.carmats.order.entity.Order;
import com.carmats.order.entity.OrderStatus;
import com.carmats.order.repository.OrderRepository;
import com.carmats.payment.dto.request.ProcessPaymentRequest;
import com.carmats.payment.dto.response.PaymentResponse;
import com.carmats.payment.entity.*;
import com.carmats.payment.mapper.PaymentMapper;
import com.carmats.payment.provider.MockPaymentProvider;
import com.carmats.payment.provider.dto.ProviderPaymentRequest;
import com.carmats.payment.provider.dto.ProviderPaymentResult;
import com.carmats.payment.provider.dto.ProviderRefundRequest;
import com.carmats.payment.provider.dto.ProviderRefundResult;
import com.carmats.payment.repository.PaymentRepository;
import com.carmats.payment.repository.PaymentTransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final OrderRepository orderRepository;
    private final MockPaymentProvider mockPaymentProvider;

    public PaymentService(
            PaymentRepository paymentRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            OrderRepository orderRepository,
            MockPaymentProvider mockPaymentProvider
    ) {
        this.paymentRepository = paymentRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.orderRepository = orderRepository;
        this.mockPaymentProvider = mockPaymentProvider;
    }

    public PaymentResponse processPayment(UUID userId, ProcessPaymentRequest request) {
        Order order;
        if (userId != null) {
            order = orderRepository.findByOrderNumberAndUserId(request.orderNumber(), userId)
                    .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Sipariş bulunamadı."));
        } else {
            order = orderRepository.findByOrderNumber(request.orderNumber())
                    .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Sipariş bulunamadı."));
        }

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.PROCESSING ||
                order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException("ORDER_ALREADY_PAID", "Bu siparişin ödemesi zaten tamamlanmıştır.");
        }

        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REFUNDED) {
            throw new BusinessException("ORDER_CANCELLED", "İptal edilmiş veya iade edilmiş sipariş için ödeme yapılamaz.");
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseGet(() -> new Payment(
                        order,
                        request.paymentMethod(),
                        PaymentProviderType.MOCK,
                        order.getGrandTotal(),
                        order.getCurrency(),
                        request.installment() != null ? request.installment() : 1
                ));

        ProviderPaymentRequest providerRequest = new ProviderPaymentRequest(
                order.getOrderNumber(),
                order.getGrandTotal(),
                order.getCurrency(),
                request.cardHolderName(),
                request.cardNumber(),
                request.expireMonth(),
                request.expireYear(),
                request.cvc(),
                request.installment() != null ? request.installment() : 1
        );

        ProviderPaymentResult result = mockPaymentProvider.processPayment(providerRequest);

        PaymentTransaction transaction = new PaymentTransaction(
                payment,
                PaymentTransactionType.PAYMENT,
                result.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED,
                order.getGrandTotal(),
                result.paymentIdExternal(),
                result.rawRequest(),
                result.rawResponse()
        );
        payment.addTransaction(transaction);

        if (result.success()) {
            payment.markSuccess(
                    result.paymentIdExternal(),
                    result.cardBin(),
                    result.cardLastFour(),
                    result.cardType(),
                    result.cardFamily()
            );
            payment.setConversationId(result.conversationId());

            order.addStatusHistory(
                    order.getStatus(),
                    OrderStatus.PAID,
                    "Ödeme onaylandı (" + request.paymentMethod() + " - " + result.cardFamily() + ")",
                    userId != null ? "CUSTOMER" : "GUEST"
            );
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
        } else {
            payment.markFailed(result.errorCode(), result.errorMessage());
            paymentRepository.save(payment);
            throw new BusinessException(result.errorCode() != null ? result.errorCode() : "PAYMENT_FAILED",
                    result.errorMessage() != null ? result.errorMessage() : "Ödeme işlemi başarısız oldu.");
        }

        Payment saved = paymentRepository.save(payment);
        return PaymentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderNumber(UUID userId, String orderNumber) {
        Order order;
        if (userId != null) {
            order = orderRepository.findByOrderNumberAndUserId(orderNumber, userId)
                    .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Sipariş bulunamadı."));
        } else {
            order = orderRepository.findByOrderNumber(orderNumber)
                    .orElseThrow(() -> new NotFoundException("ORDER_NOT_FOUND", "Sipariş bulunamadı."));
        }

        Payment payment = paymentRepository.findByOrderId(order.getId())
                .orElseThrow(() -> new NotFoundException("PAYMENT_NOT_FOUND", "Ödeme kaydı bulunamadı."));

        return PaymentMapper.toResponse(payment);
    }

    public PaymentResponse processRefund(UUID orderId, BigDecimal amount, String reason, String adminEmail) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("PAYMENT_NOT_FOUND", "Ödeme kaydı bulunamadı."));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new BusinessException("PAYMENT_NOT_ELIGIBLE_FOR_REFUND", "Sadece başarılı ödemeler iade edilebilir.");
        }

        BigDecimal refundAmount = amount != null ? amount : payment.getAmount();

        ProviderRefundRequest refundRequest = new ProviderRefundRequest(
                payment.getPaymentIdExternal(),
                refundAmount,
                payment.getCurrency(),
                reason
        );

        ProviderRefundResult result = mockPaymentProvider.processRefund(refundRequest);

        PaymentTransaction transaction = new PaymentTransaction(
                payment,
                PaymentTransactionType.REFUND,
                result.success() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED,
                refundAmount,
                result.refundIdExternal(),
                result.rawRequest(),
                result.rawResponse()
        );
        payment.addTransaction(transaction);

        if (result.success()) {
            payment.markRefunded();
            Order order = payment.getOrder();
            order.addStatusHistory(
                    order.getStatus(),
                    OrderStatus.REFUNDED,
                    "İade yapıldı: " + (reason != null ? reason : "Tutar: " + refundAmount),
                    adminEmail != null ? adminEmail : "ADMIN"
            );
            order.setStatus(OrderStatus.REFUNDED);
            orderRepository.save(order);
        } else {
            throw new BusinessException(result.errorCode() != null ? result.errorCode() : "REFUND_FAILED",
                    result.errorMessage() != null ? result.errorMessage() : "İade işlemi başarısız oldu.");
        }

        Payment saved = paymentRepository.save(payment);
        return PaymentMapper.toResponse(saved);
    }
}
