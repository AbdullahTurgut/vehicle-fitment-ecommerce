package com.carmats.catalog.entity;

import com.carmats.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "product_features")
public class ProductFeature extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String icon;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    protected ProductFeature() {
    }

    public ProductFeature(
            Product product,
            String title,
            String description
    ) {
        this.product = product;
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getIcon() {
        return icon;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}