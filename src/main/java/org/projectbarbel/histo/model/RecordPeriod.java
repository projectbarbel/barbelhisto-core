package org.projectbarbel.histo.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import org.projectbarbel.histo.BarbelHistoContext;

/**
 * The abstraction for storing the record time details of a version.
 * 
 * @author Niklas Schlimm
 *
 */
public class RecordPeriod {

    public static final ZonedDateTime NOT_INACTIVATED = ZonedDateTime.of(LocalDateTime.of(2199, 12, 31, 23, 59),
            ZoneId.of("Z"));
    public static final String NOBODY = "NOBODY";

    private ZonedDateTime createdAt;
    private String createdBy;
    private ZonedDateTime inactivatedAt;
    private String inactivatedBy;
    private BitemporalObjectState state;

    public RecordPeriod() {
        super();
    }

    private RecordPeriod(Builder builder) {
        this.createdAt = builder.createdAt != null ? builder.createdAt : BarbelHistoContext.getBarbelClock().now();
        this.createdBy = builder.createdBy != null ? builder.createdBy : BarbelHistoContext.getDefaultUser();
        this.inactivatedAt = builder.inactivatedAt != null ? builder.inactivatedAt : NOT_INACTIVATED;
        this.inactivatedBy = builder.inactivatedBy != null ? builder.inactivatedBy : NOBODY;
        this.state = compileState();
    }

    public static RecordPeriod createActive(BarbelHistoContext context) {
        return builder().createdBy(context.getUser()).createdAt(BarbelHistoContext.getBarbelClock().now()).build();
    }

    public static RecordPeriod createActive() {
    	return builder().createdBy(BarbelHistoContext.getDefaultUser()).createdAt(BarbelHistoContext.getBarbelClock().now()).build();
    }
    
    public BitemporalObjectState compileState() {
        if (inactivatedAt.equals(NOT_INACTIVATED) && inactivatedBy.equals(NOBODY))
            return BitemporalObjectState.ACTIVE;
        else if (!inactivatedAt.equals(NOT_INACTIVATED) && !inactivatedBy.equals(NOBODY))
            return BitemporalObjectState.INACTIVE;
        else
            throw new IllegalStateException("cannot compile state: " + toString());
    }

    public RecordPeriod inactivate(BarbelHistoContext context) {
        this.inactivatedAt = BarbelHistoContext.getBarbelClock().now();
        this.state = BitemporalObjectState.INACTIVE;
        this.inactivatedBy = Objects.requireNonNull(context.getUser());
        return this;
    }

    public RecordPeriod activate() {
        this.inactivatedAt = NOT_INACTIVATED;
        this.state = BitemporalObjectState.ACTIVE;
        this.inactivatedBy = NOBODY;
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
