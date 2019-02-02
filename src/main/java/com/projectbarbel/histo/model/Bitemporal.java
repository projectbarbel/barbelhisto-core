package com.projectbarbel.histo.model;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

import com.projectbarbel.histo.BarbelHistoContext;

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

    default void setBitemporalStamp(Supplier<BitemporalStamp> stampSupplier) {
        setBitemporalStamp(stampSupplier.get());
    }
    
    /**
     * The unique ID of the value object version (not the documentId). Must be
     * uniquie within the document collection/table.
     * 
     * @return version id
     */
    @SuppressWarnings("unchecked")
    default O getVersionId() {
        return (O) getBitemporalStamp().getVersionId();
    }

    default String getDocumentId() {
        return getBitemporalStamp().getDocumentId();
    }

    default LocalDate getEffectiveFrom() {
        return getBitemporalStamp().getEffectiveTime().from();
    }

    default LocalDate getEffectiveUntil() {
        return getBitemporalStamp().getEffectiveTime().until();
    }

    default boolean isEffectiveInfinitely() {
        return getEffectiveUntil().equals(BarbelHistoContext.instance().infiniteDate());
    }

    default void inactivate() {
        setBitemporalStamp(getBitemporalStamp().inactivatedCopy(getDocumentId()));
    }

    default boolean isActive() {
        return getBitemporalStamp().isActive();
    }

    default BitemporalObjectState getState() {
        return getBitemporalStamp().getRecordTime().getState();
    }

    default String getCreatedBy() {
        return getBitemporalStamp().getRecordTime().getCreatedBy();
    }

    default ZonedDateTime getCreatedAt() {
        return getBitemporalStamp().getRecordTime().getCreatedAt();
    }

    default ZonedDateTime getInactivatedAt() {
        return getBitemporalStamp().getRecordTime().getInactivatedAt();
    }

    default String prettyPrint() {
        return String.format("|%1$-40s|%2$-15tF|%3$-16tF|%4$-8s|%5$-21s|%6$-23s|%7$-21s|%8$-23s|", getVersionId(),
                getEffectiveFrom(), getEffectiveUntil(), getState().name(),
                getCreatedBy().substring(0, Math.min(getCreatedBy().length(), 20)),
                DateTimeFormatter.ofPattern("dd/MM/yyyy - hh:mm:ss").format(getCreatedAt()),
                getInactivatedBy().substring(0, Math.min(getCreatedBy().length(), 20)), getInactivatedAt());
    }

    default String getInactivatedBy() {
        return getBitemporalStamp().getRecordTime().getInactivatedBy();
    }

}
