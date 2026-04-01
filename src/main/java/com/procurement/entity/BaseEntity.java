package com.procurement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseEntity {

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "last_updated_dt")
    private LocalDateTime lastUpdatedDt;

    public void setAuditFields(String userName, boolean isNew) {
        if (isNew) {
            this.createdBy = userName;
        }
        this.updatedBy = userName;
        this.lastUpdatedDt = java.time.LocalDateTime.now();
    }

    public String getCreatedBy() { return createdBy; }
    public String getUpdatedBy() { return updatedBy; }
    public LocalDateTime getLastUpdatedDt() { return lastUpdatedDt; }
}