package com.carmats.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateReviewRequest(
        @NotNull(message = "Puan zorunludur.")
        @Min(value = 1, message = "Puan en az 1 olmalıdır.")
        @Max(value = 5, message = "Puan en fazla 5 olabilir.")
        Integer rating,

        String title,

        @NotBlank(message = "Yorum metni zorunludur.")
        String comment
) {
}
