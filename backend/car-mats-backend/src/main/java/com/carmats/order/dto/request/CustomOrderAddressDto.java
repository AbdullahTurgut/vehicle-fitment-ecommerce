package com.carmats.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomOrderAddressDto(
        @NotBlank(message = "Ad zorunludur.")
        @Size(max = 100)
        String firstName,

        @NotBlank(message = "Soyad zorunludur.")
        @Size(max = 100)
        String lastName,

        @NotBlank(message = "Telefon numarası zorunludur.")
        @Size(max = 30)
        String phoneNumber,

        @NotBlank(message = "İl zorunludur.")
        @Size(max = 100)
        String city,

        @NotBlank(message = "İlçe zorunludur.")
        @Size(max = 100)
        String district,

        @Size(max = 150)
        String neighborhood,

        @NotBlank(message = "Adres satırı zorunludur.")
        String addressLine,

        @Size(max = 20)
        String postalCode,

        @Size(max = 200)
        String companyName,

        @Size(max = 50)
        String taxNumber,

        @Size(max = 100)
        String taxOffice
) {
}
