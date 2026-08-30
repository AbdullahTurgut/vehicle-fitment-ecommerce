package com.carmats.order.entity;

import com.carmats.catalog.entity.Product;
import com.carmats.common.entity.BaseEntity;
import com.carmats.vehicle.entity.VehicleVariant;
import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "product_slug", nullable = false, length = 200)
    private String productSlug;

    @Column(name = "product_sku", nullable = false, length = 100)
    private String productSku;

    @Column(name = "primary_image_url", length = 500)
    private String primaryImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_variant_id")
    private VehicleVariant vehicleVariant;

    @Column(name = "vehicle_variant_name", length = 200)
    private String vehicleVariantName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    protected OrderItem() {
    }

    public OrderItem(
            Order order,
            Product product,
            String productName,
            String productSlug,
            String productSku,
            String primaryImageUrl,
            VehicleVariant vehicleVariant,
            String vehicleVariantName,
            int quantity,
            BigDecimal unitPrice
    ) {
        this.order = order;
        this.product = product;
        this.productName = productName;
        this.productSlug = productSlug;
        this.productSku = productSku;
        this.primaryImageUrl = primaryImageUrl;
        this.vehicleVariant = vehicleVariant;
        this.vehicleVariantName = vehicleVariantName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductSlug() {
        return productSlug;
    }

    public String getProductSku() {
        return productSku;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public VehicleVariant getVehicleVariant() {
        return vehicleVariant;
    }

    public String getVehicleVariantName() {
        return vehicleVariantName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
