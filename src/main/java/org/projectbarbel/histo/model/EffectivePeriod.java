package org.projectbarbel.histo.model;

import java.time.LocalDate;
import java.util.Objects;

import org.projectbarbel.histo.BarbelHistoContext;

/**
 * The abstraction storing the effective time for a version.
 * 
 * @author Niklas Schlimm
 *
 */
public final class EffectivePeriod {
    
    public static final LocalDate INFINITE = LocalDate.of(2199, 12, 31);
    
    private final LocalDate until;
    private final LocalDate from;

    private EffectivePeriod(LocalDate from, LocalDate until) {
        this.from = Objects.requireNonNull(from);
        this.until = Objects.requireNonNull(until);
    }

    public static EffectivePeriod of(LocalDate from, LocalDate until) {
        return new EffectivePeriod(from, until);
    }

    public static EffectivePeriod nowToInfinite() {
    	return new EffectivePeriod(BarbelHistoContext.getBarbelClock().now().toLocalDate(), LocalDate.MAX);
    }
    
    public boolean isInfinite() {
        return until.equals(BarbelHistoContext.getInfiniteDate());
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
        return Objects.equals(from, abstractValueObject.from) && Objects.equals(until, abstractValueObject.until);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, until);
    }

    @Override
    public String toString() {
        return "EffectivePeriod [from=" + from + ", until=" + until + "]";
    }

}
