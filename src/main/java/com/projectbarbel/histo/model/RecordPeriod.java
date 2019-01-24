package com.projectbarbel.histo.model;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public class RecordPeriod {

    public final static Instant NOT_INACTIVATED = Instant.ofEpochMilli(Long.MAX_VALUE);
    public final static String NOBODY = "NOBODY";

    public Instant createdAt = Clock.systemUTC().instant();
    public String createdBy = "SYSTEM";
    public Instant inactivatedAt = NOT_INACTIVATED;
    public String inactivatedBy = NOBODY;
    public BitemporalObjectState state = BitemporalObjectState.ACTIVE;

    private RecordPeriod() {
        super();
    }

    /**
     * Creates an active record time instance with given values.
     * 
     * @param createdBy
     * @param createdAt
     * @return record period
     */
    public static RecordPeriod create(String createdBy, Instant createdAt, Instant inactivatedAt, String inactivatedBy, BitemporalObjectState state) {
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
    public static RecordPeriod create(String createdBy, Instant createdAt) {
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
        this.inactivatedAt = Instant.now(Clock.systemUTC());
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

}
