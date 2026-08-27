package com.carmats.vehicle.entity;

import com.carmats.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "vehicle_variants")
public class VehicleVariant extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "generation_id", nullable = false)
    private VehicleGeneration generation;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "body_type", length = 50)
    private String bodyType;

    @Column(name = "fuel_type", length = 50)
    private String fuelType;

    @Column(name = "seat_count")
    private Integer seatCount;

    @Column(name = "trunk_type", length = 100)
    private String trunkType;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    protected VehicleVariant() {
    }

    public VehicleVariant(
            VehicleGeneration generation,
            String name
    ) {
        this.generation = generation;
        this.name = name;
    }

    public VehicleGeneration getGeneration() {
        return generation;
    }

    public String getName() {
        return name;
    }

    public String getBodyType() {
        return bodyType;
    }

    public String getFuelType() {
        return fuelType;
    }

    public Integer getSeatCount() {
        return seatCount;
    }

    public String getTrunkType() {
        return trunkType;
    }

    public String getNotes() {
        return notes;
    }

    public boolean isActive() {
        return active;
    }
}