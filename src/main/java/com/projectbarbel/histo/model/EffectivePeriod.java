package com.projectbarbel.histo.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import com.projectbarbel.histo.BarbelHistoHelper;

public class EffectivePeriod {
    public final static Instant INFINITE = Instant.ofEpochMilli(Long.MAX_VALUE);
    public final static Instant BIGBANG = Instant.ofEpochMilli(Long.MIN_VALUE);
    public Instant from = BIGBANG;
    public Instant until = INFINITE;

    private EffectivePeriod() {
        super();
    }

    /**
     * The default {@link EffectivePeriod} is always effective.
     * 
     * @return default effective period
     */
    public static EffectivePeriod create() {
        return new EffectivePeriod();
    }

    /**
     * Set effective from date. Effective period ist effective from the given value {@link LocalDate}.
     * 
     * @param from effective date
     * @return adopted effective period
     */
    public EffectivePeriod from(LocalDate from) {
        this.from = BarbelHistoHelper.effectiveDateToEffectiveUTCInstant(Objects.requireNonNull(from));
        return this;
    }

    public EffectivePeriod from(Instant from) {
        this.from = Objects.requireNonNull(from);
        return this;
    }
    
    public EffectivePeriod until(LocalDate until) {
        this.until = BarbelHistoHelper.effectiveDateToEffectiveUTCInstant(Objects.requireNonNull(until));
        return this;
    }

    public EffectivePeriod until(Instant until) {
        this.until = Objects.requireNonNull(until);
        return this;
    }
    
    public EffectivePeriod toInfinite() {
        this.until = INFINITE;
        return this;
    }

    public EffectivePeriod fromNow() {
        this.from = BarbelHistoHelper.effectiveDateToEffectiveUTCInstant(LocalDate.now());
        return this;
    }
    
    public LocalDate getEffectiveFromLocalDate() {
        return BarbelHistoHelper.effectiveInstantToEffectiveDate(Objects.requireNonNull(from));
    }
    
    public LocalDate getEffectiveUntilLocalDate() {
        return BarbelHistoHelper.effectiveInstantToEffectiveDate(Objects.requireNonNull(until));
    }
    
    public Instant getEffectiveFromInstant() {
        return from;
    }
    
    public Instant getEffectiveUntilInstant() {
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


}
