package com.carmats.catalog.entity;

import com.carmats.common.entity.BaseEntity;
import com.carmats.vehicle.entity.VehicleVariant;
import jakarta.persistence.*;

@Entity
@Table(
        name = "product_compatibilities",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_product_compatibility",
                        columnNames = {
                                "product_id",
                                "vehicle_variant_id",
                                "start_year",
                                "end_year"
                        }
                )
        }
)
public class ProductCompatibility extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_variant_id", nullable = false)
    private VehicleVariant vehicleVariant;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "end_year")
    private Integer endYear;

    @Column(columnDefinition = "TEXT")
    private String notes;

    protected ProductCompatibility() {
    }

    public ProductCompatibility(
            Product product,
            VehicleVariant vehicleVariant,
            Integer startYear,
            Integer endYear
    ) {
        this.product = product;
        this.vehicleVariant = vehicleVariant;
        this.startYear = startYear;
        this.endYear = endYear;
    }

    public ProductCompatibility(
            Product product,
            VehicleVariant vehicleVariant,
            Integer startYear,
            Integer endYear,
            String notes
    ) {
        this.product = product;
        this.vehicleVariant = vehicleVariant;
        this.startYear = startYear;
        this.endYear = endYear;
        this.notes = notes;
    }

    public Product getProduct() {
        return product;
    }

    public VehicleVariant getVehicleVariant() {
        return vehicleVariant;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public Integer getEndYear() {
        return endYear;
    }

    public String getNotes() {
        return notes;
    }
}