package com.carmats.order.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateOrderRequest(
        UUID deliveryAddressId,
        UUID billingAddressId,

        @Valid
        CustomOrderAddressDto customDeliveryAddress,

        @Valid
        CustomOrderAddressDto customBillingAddress,

        @Email(message = "Geçerli bir e-posta adresi giriniz.")
        String guestEmail,

        @Size(max = 100)
        String guestFirstName,

        @Size(max = 100)
        String guestLastName,

        @Size(max = 30)
        String guestPhoneNumber,

        String customerNotes,
        String guestToken
) {
}
