package com.carmats.catalog.repository.projection;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductListProjection {

    UUID getId();

    String getName();

    String getSlug();

    String getSku();

    BigDecimal getBasePrice();

    BigDecimal getSalePrice();

    Integer getStockQuantity();

    Boolean getFeatured();

    String getPrimaryImageUrl();
}