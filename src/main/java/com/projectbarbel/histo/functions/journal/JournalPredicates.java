package com.projectbarbel.histo.functions.journal;

import java.time.LocalDate;

import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.EffectivePeriod;

public class JournalPredicates {

    private Object constantArgument;

    public JournalPredicates(Object constantArgument) {
        this.constantArgument = constantArgument;
    }

    public JournalPredicates() {
    }

    public boolean isActive(Bitemporal<?> document) {
        return document.getBitemporalStamp().isActive();
    }

    public boolean effectiveOn(Bitemporal<?> document) {
        return document.getEffectiveFrom().equals(constantArgument)
                || (document.getEffectiveFrom().isBefore((LocalDate) constantArgument)
                        && document.getEffectiveUntil().isAfter((LocalDate) constantArgument));
    }

    public boolean effectiveAfter(Bitemporal<?> document) {
        return document.getEffectiveFrom().equals((LocalDate) constantArgument)
                || document.getEffectiveFrom().isAfter((LocalDate) constantArgument);
    }

    public boolean effectiveBetween(Bitemporal<?> document) {
        EffectivePeriod period = (EffectivePeriod) constantArgument;
        return (document.getEffectiveFrom().equals(period.from()) || document.getEffectiveFrom().isAfter(period.from()))
                && (document.getEffectiveUntil().isBefore(period.until())
                        || document.getEffectiveUntil().equals(period.until()));
    }

}
