package com.carmats.checkout.controller;

import com.carmats.checkout.dto.request.CheckoutPreviewRequest;
import com.carmats.checkout.dto.response.CheckoutSummaryResponse;
import com.carmats.checkout.dto.response.CheckoutValidationResponse;
import com.carmats.checkout.service.CheckoutService;
import com.carmats.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Ödeme ve Sipariş Öncesi (Checkout)", description = "Sipariş özeti, kargo ve tutar hesaplaması, adres doğrulaması")
@RestController
@RequestMapping("/api/v1/checkout")
@Validated
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @Operation(summary = "Checkout özeti ve tutar hesaplama", description = "Sepet tutarı, kargo ücreti, adres ve sipariş dökümünü hesaplayarak döner.")
    @PostMapping("/preview")
    public ResponseEntity<CheckoutSummaryResponse> preview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @RequestBody(required = false) CheckoutPreviewRequest request
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(checkoutService.getCheckoutSummary(userId, guestToken, request));
    }

    @Operation(summary = "Checkout ön doğrulama", description = "Stok, ürün durumu ve adres geçerliliğini sipariş öncesi test eder.")
    @PostMapping("/validate")
    public ResponseEntity<CheckoutValidationResponse> validate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestHeader(value = "X-Guest-Token", required = false) String guestToken,
            @RequestBody(required = false) CheckoutPreviewRequest request
    ) {
        UUID userId = userDetails != null ? userDetails.getId() : null;
        return ResponseEntity.ok(checkoutService.validateCheckout(userId, guestToken, request));
    }
}
