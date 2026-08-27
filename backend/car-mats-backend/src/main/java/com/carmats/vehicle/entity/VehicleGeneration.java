package com.carmats.vehicle.entity;

import com.carmats.common.entity.BaseEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "vehicle_generations")
public class VehicleGeneration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "model_id", nullable = false)
    private VehicleModel model;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String code;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "end_year")
    private Integer endYear;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    protected VehicleGeneration() {
    }

    public VehicleGeneration(
            VehicleModel model,
            String name,
            String code,
            Integer startYear,
            Integer endYear
    ) {
        this.model = model;
        this.name = name;
        this.code = code;
        this.startYear = startYear;
        this.endYear = endYear;
    }

    public VehicleModel getModel() {
        return model;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public Integer getStartYear() {
        return startYear;
    }

    public Integer getEndYear() {
        return endYear;
    }

    public boolean isActive() {
        return active;
    }
}