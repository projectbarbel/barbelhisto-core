package com.projectbarbel.histo.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import com.projectbarbel.histo.BarbelHistoContext;

public class RecordPeriod {

    private final static ZonedDateTime NOT_INACTIVATED = ZonedDateTime.of(LocalDateTime.of(2199, 12, 31, 23, 59),
            ZoneId.of("Z"));
    private final static String NOBODY = "NOBODY";

    private final ZonedDateTime createdAt;
    private final String createdBy;
    private ZonedDateTime inactivatedAt;
    private String inactivatedBy;
    private BitemporalObjectState state;

    private RecordPeriod(Builder builder) {
        this.createdAt = builder.createdAt != null ? builder.createdAt : BarbelHistoContext.getClock().now();
        this.createdBy = builder.createdBy != null ? builder.createdBy : BarbelHistoContext.getDefaultCreatedBy();
        this.inactivatedAt = builder.inactivatedAt != null ? builder.inactivatedAt : NOT_INACTIVATED;
        this.inactivatedBy = builder.inactivatedBy != null ? builder.inactivatedBy : NOBODY;
        this.state = compileState();
    }

    private BitemporalObjectState compileState() {
        if (inactivatedAt.equals(NOT_INACTIVATED) && inactivatedBy.equals(NOBODY))
            return BitemporalObjectState.ACTIVE;
        else if(!inactivatedAt.equals(NOT_INACTIVATED) && !inactivatedBy.equals(NOBODY))
            return BitemporalObjectState.INACTIVE;
        else
            throw new IllegalStateException("cannot compile state: " + toString());
    }

    /**
     * Inactivates this record time instance with given value.
     * 
     * @param createdBy
     * @param createdAt
     * @return record period
     */
    public RecordPeriod inactivate(String inactivatedBy) {
        this.inactivatedAt = BarbelHistoContext.getClock().now();
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
        return "RecordPeriod [createdAt=" + createdAt + ", createdBy=" + createdBy + ", inactivatedAt=" + inactivatedAt
                + ", inactivatedBy=" + inactivatedBy + ", state=" + state + "]";
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public BitemporalObjectState getState() {
        return state;
    }

    public ZonedDateTime getInactivatedAt() {
        return inactivatedAt;
    }

    public String getInactivatedBy() {
        return inactivatedBy;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private ZonedDateTime createdAt;
        private String createdBy;
        private ZonedDateTime inactivatedAt;
        private String inactivatedBy;

        private Builder() {
        }

        public Builder createdAt(ZonedDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder inactivatedAt(ZonedDateTime inactivatedAt) {
            this.inactivatedAt = inactivatedAt;
            return this;
        }

        public Builder inactivatedBy(String inactivatedBy) {
            this.inactivatedBy = inactivatedBy;
            return this;
        }

        public RecordPeriod build() {
            return new RecordPeriod(this);
        }
    }

}
