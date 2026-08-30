package com.carmats.favorite.mapper;

import com.carmats.favorite.dto.response.FavoriteResponse;
import com.carmats.favorite.entity.Favorite;

public final class FavoriteMapper {

    private FavoriteMapper() {
    }

    public static FavoriteResponse toResponse(Favorite favorite) {
        if (favorite == null) {
            return null;
        }

        return new FavoriteResponse(
                favorite.getId(),
                favorite.getProduct() != null ? favorite.getProduct().getId() : null,
                favorite.getProduct() != null ? favorite.getProduct().getName() : null,
                favorite.getProduct() != null ? favorite.getProduct().getSlug() : null,
                favorite.getProduct() != null ? favorite.getProduct().getSku() : null,
                favorite.getProduct() != null ? favorite.getProduct().getBasePrice() : null,
                favorite.getProduct() != null ? favorite.getProduct().getSalePrice() : null,
                favorite.getProduct() != null && favorite.getProduct().getCategory() != null ? favorite.getProduct().getCategory().getName() : null,
                favorite.getCreatedAt()
        );
    }
}
