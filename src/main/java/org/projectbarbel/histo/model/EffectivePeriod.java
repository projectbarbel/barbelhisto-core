package org.projectbarbel.histo.model;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

import org.projectbarbel.histo.BarbelHistoContext;

/**
 * The abstraction storing the effective time for a version.
 * 
 * @author Niklas Schlimm
 *
 */
public final class EffectivePeriod {

    public static final ZonedDateTime INFINITE = ZonedDateTime.of(LocalDateTime.of(2199, 12, 31, 23, 59),
            ZoneId.of("Z"));

    private final ZonedDateTime until;
    private final ZonedDateTime from;

    private EffectivePeriod(ZonedDateTime from, ZonedDateTime until) {
        this.from = Objects.requireNonNull(from);
        this.until = Objects.requireNonNull(until);
    }

    public static EffectivePeriod of(ZonedDateTime from, ZonedDateTime until) {
        return new EffectivePeriod(from, until);
    }

    public static EffectivePeriod nowToInfinite() {
    	return new EffectivePeriod(BarbelHistoContext.getBarbelClock().now(), INFINITE);
    }
    
    public boolean isInfinite() {
        return until.equals(INFINITE);
    }

    public ZonedDateTime from() {
        return from;
    }

    public ZonedDateTime until() {
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
