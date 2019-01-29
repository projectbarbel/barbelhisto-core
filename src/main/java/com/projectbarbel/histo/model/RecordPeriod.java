package com.projectbarbel.histo.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

public class RecordPeriod {

    public final static LocalDateTime NOT_INACTIVATED = LocalDateTime.of(2199,12,31,23,59);
    public final static String NOBODY = "NOBODY";

    private Systemclock clock = new Systemclock();
    private ZoneId zone = ZoneId.systemDefault();
    private LocalDateTime createdAt = clock.now();
    private String createdBy = "SYSTEM";
    private LocalDateTime inactivatedAt = NOT_INACTIVATED; 
    private String inactivatedBy = NOBODY;
    private BitemporalObjectState state = BitemporalObjectState.ACTIVE;

    private RecordPeriod() {
        super();
    }

    public ZoneId getZone() {
        return zone;
    }
    
    public static RecordPeriod create(String createdBy, LocalDateTime createdAt, LocalDateTime inactivatedAt, String inactivatedBy, BitemporalObjectState state) {
        RecordPeriod rp = new RecordPeriod();
        rp.createdBy = Objects.requireNonNull(createdBy);
        rp.createdAt = Objects.requireNonNull(createdAt);
        rp.inactivatedAt = Objects.requireNonNull(inactivatedAt);
        rp.inactivatedBy = Objects.requireNonNull(inactivatedBy);
        rp.state = state;
        return rp;
    }

    /**
     * Creates an active record time instance with given values.
     * 
     * @param createdBy
     * @param createdAt
     * @return record period
     */
    public static RecordPeriod create(String createdBy, LocalDateTime createdAt) {
        RecordPeriod rp = new RecordPeriod();
        rp.createdBy = Objects.requireNonNull(createdBy);
        rp.createdAt = Objects.requireNonNull(createdAt);
        return rp;
    }

    /**
     * Creates an active record time instance with given value and createdAt now.
     * 
     * @param createdBy
     * @return record period
     */
    public static RecordPeriod create(String createdBy) {
        RecordPeriod rp = new RecordPeriod();
        rp.createdBy = Objects.requireNonNull(createdBy);
        return rp;
    }

    /**
     * Inactivates this record time instance with given value.
     * 
     * @param createdBy
     * @param createdAt
     * @return record period
     */
    public RecordPeriod inactivate(String inactivatedBy) {
        this.inactivatedAt = clock.now();
        this.state = BitemporalObjectState.INACTIVE;
        this.inactivatedBy = Objects.requireNonNull(inactivatedBy);
        return this;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof RecordPeriod)) {
            return false;
        }
        RecordPeriod abstractValueObject = (RecordPeriod) o;
        return Objects.equals(createdAt, abstractValueObject.createdAt)
                && Objects.equals(createdBy, abstractValueObject.createdBy)
                && Objects.equals(inactivatedAt, abstractValueObject.inactivatedAt)
                && Objects.equals(inactivatedBy, abstractValueObject.inactivatedBy)
                && Objects.equals(state, abstractValueObject.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(createdAt, createdBy, inactivatedAt, inactivatedBy, state);
    }
    
    @Override
    public String toString() {
        return "RecordPeriod [clock=" + clock + ", zone=" + zone + ", createdAt=" + createdAt + ", createdBy="
                + createdBy + ", inactivatedAt=" + inactivatedAt + ", inactivatedBy=" + inactivatedBy + ", state="
                + state + "]";
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public BitemporalObjectState getState() {
        return state;
    }

    public LocalDateTime getInactivatedAt() {
        return inactivatedAt;
    }

    public String getInactivatedBy() {
        return inactivatedBy;
    }

}
