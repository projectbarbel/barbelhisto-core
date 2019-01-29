package com.projectbarbel.histo.functions.journal;

import java.time.LocalDate;

import com.projectbarbel.histo.model.Bitemporal;

public class JournalPredicates {

    private Object constantArgument;
    
    public JournalPredicates(Object constantArgument) {
        this.constantArgument = constantArgument;
    }
    
    public JournalPredicates() {}
    
    public boolean isActive(Bitemporal<?> document) {
        return document.getBitemporalStamp().isActive();
    }

    public boolean effectiveOn(Bitemporal<?> document) {
        return document.getEffectiveFrom().equals(constantArgument)
                || (document.getEffectiveFrom().isBefore((LocalDate)constantArgument)
                        && document.getEffectiveUntil().isAfter((LocalDate)constantArgument));
    }

    public boolean effectiveAfter(Bitemporal<?> document) {
        return document.getEffectiveFrom().equals((LocalDate)constantArgument)
                || document.getEffectiveFrom().isAfter((LocalDate)constantArgument);
    }

}
