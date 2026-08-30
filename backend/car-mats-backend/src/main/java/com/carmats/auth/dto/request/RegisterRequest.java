package com.carmats.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "E-posta adresi boş olamaz.")
        @Email(message = "Geçerli bir e-posta adresi giriniz.")
        @Size(max = 150, message = "E-posta en fazla 150 karakter olabilir.")
        String email,

        @NotBlank(message = "Şifre boş olamaz.")
        @Size(min = 6, max = 100, message = "Şifre en az 6, en fazla 100 karakter olmalıdır.")
        String password,

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
