package com.carmats.payment.provider;

import com.carmats.payment.entity.PaymentProviderType;
import com.carmats.payment.provider.dto.ProviderPaymentRequest;
import com.carmats.payment.provider.dto.ProviderPaymentResult;
import com.carmats.payment.provider.dto.ProviderRefundRequest;
import com.carmats.payment.provider.dto.ProviderRefundResult;

public interface PaymentProvider {

    PaymentProviderType getProviderType();

    ProviderPaymentResult processPayment(ProviderPaymentRequest request);

    ProviderRefundResult processRefund(ProviderRefundRequest request);
}
