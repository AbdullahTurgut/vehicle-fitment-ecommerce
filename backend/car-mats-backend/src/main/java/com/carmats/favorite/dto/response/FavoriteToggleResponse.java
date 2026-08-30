package com.carmats.favorite.dto.response;

import java.util.UUID;

public record FavoriteToggleResponse(
        UUID productId,
        boolean isFavorite,
        String message
) {
}
