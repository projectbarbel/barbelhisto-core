package com.projectbarbel.histo.model;

import java.time.LocalDate;
import java.util.function.BiFunction;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.BarbelHistoHelper;

public class VersionUpdate {
    
    private final Bitemporal<?> oldVersion;
    private Bitemporal<?> newPrecedingVersion;
    private Bitemporal<?> newSubsequentVersion;
    private LocalDate newEffectiveDate;
    private String activity = "SYSTEM_ACTIVITY";
    private String createdBy = "SYSTEM";
    private final BiFunction<Bitemporal<?>, BitemporalStamp, Bitemporal<?>> copyFunction;
    
    public static VersionUpdate of(Bitemporal<?> bitemporal, BiFunction<Bitemporal<?>, BitemporalStamp, Bitemporal<?>> copyFunction) {
        return new VersionUpdate(bitemporal, copyFunction);
    }
    
    public VersionUpdate(Bitemporal<?> bitemporal, BiFunction<Bitemporal<?>, BitemporalStamp, Bitemporal<?>> copyFunction) {
       oldVersion = bitemporal;
       newEffectiveDate = bitemporal.getEffectiveFrom();
       this.copyFunction = copyFunction;
    }

    public VersionUpdate inActivity(String activity) {
       this.activity = activity;
       return this;
    }
    
    public VersionUpdate createdBy(String user) {
        this.createdBy = user;
        return this;
    }
    
    public VersionUpdate effectiveFrom(LocalDate newEffectiveDate) {
        Validate.isTrue(newEffectiveDate.isBefore(oldVersion.getEffectiveUntil()),
                "effective date must be before current versions effective until");
        Validate.isTrue(newEffectiveDate.isBefore(LocalDate.MAX),
                "effective date cannot be infinite");
        Validate.inclusiveBetween(oldVersion.getEffectiveFromInstant().toEpochMilli(),
                oldVersion.getEffectiveUntilInstant().toEpochMilli(),
                BarbelHistoHelper.effectiveDateToEffectiveUTCInstant(newEffectiveDate).toEpochMilli(),
                "effective date of new version must be withing effective period of current version");
       this.newEffectiveDate = newEffectiveDate;
       return this;
    }
    
    public VersionUpdate execute() {
        BitemporalStamp newPrecedingStamp = BitemporalStamp.of(activity, oldVersion.getDocumentId(),
                EffectivePeriod.create().from(oldVersion.getEffectiveFromInstant()).until(newEffectiveDate),
                RecordPeriod.create(createdBy));
        BitemporalStamp newSubsequentStamp = BitemporalStamp.of(activity, oldVersion.getDocumentId(),
                EffectivePeriod.create().from(newEffectiveDate).until(oldVersion.getEffectiveUntilInstant()),
                RecordPeriod.create(createdBy));
        newPrecedingVersion = copyFunction.apply(oldVersion, newPrecedingStamp);
        newSubsequentVersion = copyFunction.apply(oldVersion, newSubsequentStamp);
        oldVersion.inactivate();
        return this;
    }
    
    public Bitemporal<?> inactivatedVersion() {
        return oldVersion;
    }
    
    public Bitemporal<?> precedingVersion() {
        return newPrecedingVersion;
    }

    public Bitemporal<?> subsequentVersion() {
        return newSubsequentVersion;
    }

}
