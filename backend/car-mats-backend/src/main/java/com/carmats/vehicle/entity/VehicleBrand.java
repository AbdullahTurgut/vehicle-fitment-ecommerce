package com.carmats.vehicle.entity;

import com.carmats.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "vehicle_brands")
public class VehicleBrand extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    protected VehicleBrand() {
    }

    public VehicleBrand(
            String name,
            String slug,
            String logoUrl,
            boolean active,
            int sortOrder
    ) {
        this.name = name;
        this.slug = slug;
        this.logoUrl = logoUrl;
        this.active = active;
        this.sortOrder = sortOrder;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public boolean isActive() {
        return active;
    }

    public int getSortOrder() {
        return sortOrder;
    }
}