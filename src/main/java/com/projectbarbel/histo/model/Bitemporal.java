package com.projectbarbel.histo.model;

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

    default LocalDate getEffectiveFrom() {
        return getBitemporalStamp().getEffectiveTime().getFrom();
    }

    default LocalDate getEffectiveUntil() {
        return getBitemporalStamp().getEffectiveTime().getUntil();
    }
    
    default boolean isEffectiveInfinitely() {
        return getEffectiveUntil().equals(EffectivePeriod.INFINITE);
    }

    default void inactivate() {
       setBitemporalStamp(getBitemporalStamp().inactivatedCopy(getDocumentId()));
    }
    
    default boolean isActive() {
        return getBitemporalStamp().isActive();
    }

}
