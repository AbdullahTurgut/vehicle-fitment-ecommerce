package com.carmats.catalog.dto.request;

import jakarta.validation.constraints.NotNull;

public record UpdateCategoryStatusRequest(
        @NotNull(message = "Aktiflik durumu belirtilmelidir.")
        Boolean active
) {
}
