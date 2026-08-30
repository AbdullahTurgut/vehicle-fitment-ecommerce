package com.carmats.payment.controller;

import com.carmats.config.security.CustomUserDetails;
import com.carmats.payment.dto.request.ProcessPaymentRequest;
import com.carmats.payment.dto.response.PaymentResponse;
import com.carmats.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProcessPaymentRequest request
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        PaymentResponse response = paymentService.processPayment(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders/{orderNumber}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderNumber(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String orderNumber
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        PaymentResponse response = paymentService.getPaymentByOrderNumber(userId, orderNumber);
        return ResponseEntity.ok(response);
    }
}
