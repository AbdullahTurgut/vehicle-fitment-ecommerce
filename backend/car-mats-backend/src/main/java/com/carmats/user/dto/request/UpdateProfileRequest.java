package com.carmats.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Ad boş olamaz.")
        @Size(max = 100, message = "Ad en fazla 100 karakter olabilir.")
        String firstName,

        @NotBlank(message = "Soyad boş olamaz.")
        @Size(max = 100, message = "Soyad en fazla 100 karakter olabilir.")
        String lastName,

        @Size(max = 30, message = "Telefon numarası en fazla 30 karakter olabilir.")
        String phoneNumber
) {
}
