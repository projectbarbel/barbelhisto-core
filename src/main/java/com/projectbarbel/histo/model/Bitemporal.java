package com.projectbarbel.histo.model;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Value Objects in the application must implement this interface.
 * 
 * @author niklasschlimm
 *
 * @param <O> the unique object identifier type of the value object
 */
public interface Bitemporal<O> {

    BitemporalStamp getBitemporalStamp();
    void setBitemporalStamp(BitemporalStamp stamp);

    /**
     * The unique ID of the value object version (not the documentId). Must be
     * uniquie within the document collection/table.
     * 
     * @return version id
     */
    @SuppressWarnings("unchecked")
    default O getVersionId() {
        return (O)getBitemporalStamp().getVersionId();
    }

    default String getDocumentId() {
        return getBitemporalStamp().getDocumentId();
    }

    default Instant getEffectiveFromInstant() {
        return getBitemporalStamp().getEffectiveTime().getEffectiveFromInstant();
    }

    default LocalDate getEffectiveFrom() {
        return getBitemporalStamp().getEffectiveTime().getEffectiveFromLocalDate();
    }

    default Instant getEffectiveUntilInstant() {
        return getBitemporalStamp().getEffectiveTime().getEffectiveUntilInstant();
    }

    default LocalDate getEffectiveUntil() {
        return getBitemporalStamp().getEffectiveTime().getEffectiveUntilLocalDate();
    }
    
    default boolean isEffectiveInfinitely() {
        return getEffectiveUntilInstant().equals(EffectivePeriod.INFINITE);
    }

    default void inactivate() {
       setBitemporalStamp(getBitemporalStamp().inactivatedCopy(getDocumentId()));
    }

}
