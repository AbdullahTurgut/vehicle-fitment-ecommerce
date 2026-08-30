package com.carmats.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAddressRequest(
        @NotBlank(message = "Adres başlığı boş olamaz.")
        @Size(max = 100, message = "Adres başlığı en fazla 100 karakter olabilir.")
        String title,

        @NotBlank(message = "Ad boş olamaz.")
        @Size(max = 100, message = "Ad en fazla 100 karakter olabilir.")
        String firstName,

        @NotBlank(message = "Soyad boş olamaz.")
        @Size(max = 100, message = "Soyad en fazla 100 karakter olabilir.")
        String lastName,

        @NotBlank(message = "Telefon numarası boş olamaz.")
        @Size(max = 30, message = "Telefon numarası en fazla 30 karakter olabilir.")
        String phoneNumber,

        @NotBlank(message = "İl boş olamaz.")
        @Size(max = 100, message = "İl en fazla 100 karakter olabilir.")
        String city,

        @NotBlank(message = "İlçe boş olamaz.")
        @Size(max = 100, message = "İlçe en fazla 100 karakter olabilir.")
        String district,

        @Size(max = 150, message = "Mahalle en fazla 150 karakter olabilir.")
        String neighborhood,

        @NotBlank(message = "Açık adres boş olamaz.")
        @Size(max = 500, message = "Açık adres en fazla 500 karakter olabilir.")
        String addressLine,

        @Size(max = 20, message = "Posta kodu en fazla 20 karakter olabilir.")
        String postalCode,

        @Size(max = 150, message = "Firma adı en fazla 150 karakter olabilir.")
        String companyName,

        @Size(max = 50, message = "Vergi numarası en fazla 50 karakter olabilir.")
        String taxNumber,

        @Size(max = 100, message = "Vergi dairesi en fazla 100 karakter olabilir.")
        String taxOffice,

        Boolean defaultDelivery,

        Boolean defaultBilling
) {
}
