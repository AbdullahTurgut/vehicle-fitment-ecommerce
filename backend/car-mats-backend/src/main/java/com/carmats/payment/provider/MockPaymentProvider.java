package com.carmats.payment.provider;

import com.carmats.payment.entity.PaymentProviderType;
import com.carmats.payment.provider.dto.ProviderPaymentRequest;
import com.carmats.payment.provider.dto.ProviderPaymentResult;
import com.carmats.payment.provider.dto.ProviderRefundRequest;
import com.carmats.payment.provider.dto.ProviderRefundResult;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MockPaymentProvider implements PaymentProvider {

    @Override
    public PaymentProviderType getProviderType() {
        return PaymentProviderType.MOCK;
    }

    @Override
    public ProviderPaymentResult processPayment(ProviderPaymentRequest request) {
        String cleanCard = request.cardNumber() != null ? request.cardNumber().replaceAll("\\s+", "") : "";

        String rawReq = String.format("{\"orderNumber\":\"%s\",\"amount\":%s,\"currency\":\"%s\",\"installment\":%d}",
                request.orderNumber(), request.amount(), request.currency(), request.installment());

        // Simulate failure conditions
        if (cleanCard.endsWith("0002") || cleanCard.endsWith("9999")) {
            String rawResp = "{\"status\":\"failure\",\"errorCode\":\"CARD_DECLINED\",\"errorMessage\":\"Kart reddedildi / Yetersiz bakiye.\"}";
            return ProviderPaymentResult.failure("CARD_DECLINED", "Kart reddedildi / Yetersiz bakiye.", rawReq, rawResp);
        }

        if (cleanCard.length() < 15 || cleanCard.length() > 19) {
            String rawResp = "{\"status\":\"failure\",\"errorCode\":\"INVALID_CARD_NUMBER\",\"errorMessage\":\"Geçersiz kart numarası.\"}";
            return ProviderPaymentResult.failure("INVALID_CARD_NUMBER", "Geçersiz kart numarası.", rawReq, rawResp);
        }

        String cardBin = cleanCard.substring(0, 6);
        String cardLastFour = cleanCard.substring(cleanCard.length() - 4);
        String paymentId = "MOCK-PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String convId = "CONV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String cardFamily = cleanCard.startsWith("4") ? "Visa" : cleanCard.startsWith("5") ? "MasterCard" : "Troy";

        String rawResp = String.format("{\"status\":\"success\",\"paymentId\":\"%s\",\"conversationId\":\"%s\",\"cardFamily\":\"%s\"}",
                paymentId, convId, cardFamily);

        return ProviderPaymentResult.success(
                paymentId,
                convId,
                request.amount(),
                cardBin,
                cardLastFour,
                "CREDIT_CARD",
                cardFamily,
                rawReq,
                rawResp
        );
    }

    @Override
    public ProviderRefundResult processRefund(ProviderRefundRequest request) {
        String rawReq = String.format("{\"paymentId\":\"%s\",\"amount\":%s,\"currency\":\"%s\"}",
                request.paymentIdExternal(), request.amount(), request.currency());
        String refundId = "MOCK-REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String rawResp = String.format("{\"status\":\"success\",\"refundId\":\"%s\"}", refundId);

        return ProviderRefundResult.success(refundId, request.amount(), rawReq, rawResp);
    }
}
