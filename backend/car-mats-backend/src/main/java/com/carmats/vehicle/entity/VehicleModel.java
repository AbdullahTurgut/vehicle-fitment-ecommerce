package com.carmats.vehicle.entity;

import com.carmats.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "vehicle_models")
public class VehicleModel extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private VehicleBrand brand;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 120)
    private String slug;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    protected VehicleModel() {
    }

    public VehicleModel(
            VehicleBrand brand,
            String name,
            String slug
    ) {
        this.brand = brand;
        this.name = name;
        this.slug = slug;
    }

    public VehicleBrand getBrand() {
        return brand;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public boolean isActive() {
        return active;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}