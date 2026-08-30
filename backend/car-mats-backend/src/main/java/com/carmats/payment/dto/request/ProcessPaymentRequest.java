package com.carmats.payment.dto.request;

import com.carmats.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ProcessPaymentRequest(
        @NotBlank(message = "Sipariş numarası zorunludur.")
        String orderNumber,

        @NotNull(message = "Ödeme yöntemi zorunludur.")
        PaymentMethod paymentMethod,

        @NotBlank(message = "Kart üzerindeki isim zorunludur.")
        String cardHolderName,

        @NotBlank(message = "Kart numarası zorunludur.")
        @Pattern(regexp = "^[0-9 ]{15,19}$", message = "Geçersiz kart numarası formatı.")
        String cardNumber,

        @NotBlank(message = "Son kullanma ayı zorunludur.")
        @Pattern(regexp = "^(0[1-9]|1[0-2])$", message = "Ay 01-12 arasında olmalıdır.")
        String expireMonth,

        @NotBlank(message = "Son kullanma yılı zorunludur.")
        @Pattern(regexp = "^[0-9]{2,4}$", message = "Geçersiz yıl.")
        String expireYear,

        @NotBlank(message = "CVC kodu zorunludur.")
        @Pattern(regexp = "^[0-9]{3,4}$", message = "CVC 3 veya 4 haneli olmalıdır.")
        String cvc,

        Integer installment
) {
}
