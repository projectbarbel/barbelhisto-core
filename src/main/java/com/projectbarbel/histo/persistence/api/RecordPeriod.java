package com.projectbarbel.histo.persistence.api;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class RecordPeriod {

    private ZoneId zone;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime inactivatedAt; 
    private String inactivatedBy;
    private String state;
    public ZoneId getZone() {
        return zone;
    }
    public void setZone(ZoneId zone) {
        this.zone = zone;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    public String getCreatedBy() {
        return createdBy;
    }
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    public LocalDateTime getInactivatedAt() {
        return inactivatedAt;
    }
    public void setInactivatedAt(LocalDateTime inactivatedAt) {
        this.inactivatedAt = inactivatedAt;
    }
    public String getInactivatedBy() {
        return inactivatedBy;
    }
    public void setInactivatedBy(String inactivatedBy) {
        this.inactivatedBy = inactivatedBy;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }

    
}
