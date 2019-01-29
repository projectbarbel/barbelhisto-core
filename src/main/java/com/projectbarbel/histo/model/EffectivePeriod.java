package com.projectbarbel.histo.model;

import java.time.LocalDate;
import java.util.Objects;

public class EffectivePeriod {
    public final static LocalDate INFINITE = LocalDate.MAX;
    public final static LocalDate BIGBANG = LocalDate.MIN;
    private LocalDate from = BIGBANG;
    private LocalDate until = INFINITE;
    private Systemclock clock = new Systemclock();
    
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
        this.from = Objects.requireNonNull(from);
        return this;
    }

    public EffectivePeriod until(LocalDate until) {
        this.until = Objects.requireNonNull(until);
        return this;
    }

    public EffectivePeriod toInfinite() {
        this.until = INFINITE;
        return this;
    }

    public boolean isInfinite() {
       return from.equals(INFINITE);
    }
    
    public EffectivePeriod fromNow() {
        this.from = clock.now().toLocalDate();
        return this;
    }
    
    public LocalDate getFrom() {
        return from;
    }

    public LocalDate getUntil() {
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
        return "EffectivePeriod [from=" + from + ", until=" + until + ", clock=" + clock + "]";
    }

}
