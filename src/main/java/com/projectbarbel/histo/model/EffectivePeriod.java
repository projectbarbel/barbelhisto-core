package com.projectbarbel.histo.model;

import java.time.LocalDate;
import java.util.Objects;

import javax.annotation.Generated;

import com.projectbarbel.histo.BarbelHistoContext;

public class EffectivePeriod {
    private final LocalDate until;
    private final LocalDate from;

    private EffectivePeriod(Builder builder) {
        this.until = builder.until != null ? builder.until : BarbelHistoContext.getInfiniteDate();
        this.from = builder.from != null ? builder.from : BarbelHistoContext.getClock().now().toLocalDate();
    }
    
    public boolean isInfinite() {
       return from.equals(BarbelHistoContext.getInfiniteDate());
    }
    
    public LocalDate from() {
        return from;
    }

    public LocalDate until() {
        return until;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof EffectivePeriod)) {
            return false;
        }
        EffectivePeriod abstractValueObject = (EffectivePeriod) o;
        return Objects.equals(from, abstractValueObject.from)
                && Objects.equals(until, abstractValueObject.until);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, until);
    }

    @Override
    public String toString() {
        return "EffectivePeriod [from=" + from + ", until=" + until + "]";
    }

    /**
     * Creates builder to build {@link EffectivePeriod}.
     * @return created builder
     */
    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private LocalDate until;
        private LocalDate from;

        private Builder() {
        }

        public Builder until(LocalDate until) {
            this.until = until;
            return this;
        }

        public Builder toInfinite() {
            this.until = BarbelHistoContext.getInfiniteDate();
            return this;
        }
        
        public Builder from(LocalDate from) {
            this.from = from;
            return this;
        }

        public Builder fromNow() {
            this.from = BarbelHistoContext.getClock().now().toLocalDate();
            return this;
        }
        
        public EffectivePeriod build() {
            return new EffectivePeriod(this);
        }
    }

}
