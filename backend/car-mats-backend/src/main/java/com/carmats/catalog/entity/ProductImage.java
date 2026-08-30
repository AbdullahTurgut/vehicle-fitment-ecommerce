package com.carmats.catalog.entity;

import com.carmats.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "product_images")
public class ProductImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "alt_text", length = 250)
    private String altText;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "is_primary", nullable = false)
    private boolean primary = false;

    protected ProductImage() {
    }

    public ProductImage(
            Product product,
            String url,
            boolean primary
    ) {
        this.product = product;
        this.url = url;
        this.primary = primary;
    }

    public Product getProduct() {
        return product;
    }

    public String getUrl() {
        return url;
    }

    public String getAltText() {
        return altText;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public boolean isPrimary() {
        return primary;
    }
}