package com.carmats.catalog.entity;

import com.carmats.common.entity.BaseEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 220)
    private String slug;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "short_description", length = 500)
    private String shortDescription;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "sale_price", precision = 12, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(nullable = false)
    private boolean featured = false;

    @Column(name = "manufacturer_brand", length = 120)
    private String manufacturerBrand;

    @Column(length = 120)
    private String material;

    protected Product() {
    }

    public Product(
            Category category,
            String name,
            String slug,
            String sku,
            BigDecimal basePrice,
            int stockQuantity
    ) {
        this.category = category;
        this.name = name;
        this.slug = slug;
        this.sku = sku;
        this.basePrice = basePrice;
        this.stockQuantity = stockQuantity;
    }

    public Product(
            Category category,
            String name,
            String slug,
            String sku,
            String shortDescription,
            String description,
            BigDecimal basePrice,
            BigDecimal salePrice,
            int stockQuantity,
            ProductStatus status,
            boolean featured,
            String manufacturerBrand,
            String material
    ) {
        this.category = category;
        this.name = name;
        this.slug = slug;
        this.sku = sku;
        this.shortDescription = shortDescription;
        this.description = description;
        this.basePrice = basePrice;
        this.salePrice = salePrice;
        this.stockQuantity = stockQuantity;
        this.status = status != null ? status : ProductStatus.DRAFT;
        this.featured = featured;
        this.manufacturerBrand = manufacturerBrand;
        this.material = material;
    }

    public void update(
            Category category,
            String name,
            String slug,
            String sku,
            String shortDescription,
            String description,
            BigDecimal basePrice,
            BigDecimal salePrice,
            int stockQuantity,
            boolean featured,
            String manufacturerBrand,
            String material
    ) {
        this.category = category;
        this.name = name;
        this.slug = slug;
        this.sku = sku;
        this.shortDescription = shortDescription;
        this.description = description;
        this.basePrice = basePrice;
        this.salePrice = salePrice;
        this.stockQuantity = stockQuantity;
        this.featured = featured;
        this.manufacturerBrand = manufacturerBrand;
        this.material = material;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public Category getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getSku() {
        return sku;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public boolean isFeatured() {
        return featured;
    }

    public String getManufacturerBrand() {
        return manufacturerBrand;
    }

    public String getMaterial() {
        return material;
    }
    @Transient
    public BigDecimal getEffectivePrice() {

        return salePrice != null
                ? salePrice
                : basePrice;
    }

    @Transient
    public boolean isInStock() {

        return stockQuantity > 0
                && status == ProductStatus.ACTIVE;
    }
}