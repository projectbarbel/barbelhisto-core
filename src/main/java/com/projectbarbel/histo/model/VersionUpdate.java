package com.projectbarbel.histo.model;

import java.time.LocalDate;
import java.util.function.BiFunction;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelHistoFactory.HistoType;
import com.projectbarbel.histo.BarbelHistoHelper;

public class VersionUpdate implements Updater {
    
    private final Bitemporal<?> oldVersion;
    private Bitemporal<?> newPrecedingVersion;
    private Bitemporal<?> newSubsequentVersion;
    private LocalDate newEffectiveDate;
    private String activity = "SYSTEM_ACTIVITY";
    private String createdBy = "SYSTEM";
    
    private final static BiFunction<Bitemporal<?>, BitemporalStamp, Bitemporal<?>> copier = BarbelHistoFactory
            .instanceOf(HistoType.COPIER);

    public static Updater of(Bitemporal<?> bitemporal) {
        return new VersionUpdate(bitemporal);
    }
    
    private VersionUpdate(Bitemporal<?> bitemporal) {
       oldVersion = bitemporal;
       newEffectiveDate = bitemporal.getEffectiveFrom();
    }

    /* (non-Javadoc)
     * @see com.projectbarbel.histo.model.Updater#inActivity(java.lang.String)
     */
    @Override
    public Updater inActivity(String activity) {
       this.activity = activity;
       return this;
    }
    
    /* (non-Javadoc)
     * @see com.projectbarbel.histo.model.Updater#createdBy(java.lang.String)
     */
    @Override
    public Updater createdBy(String user) {
        this.createdBy = user;
        return this;
    }
    
    /* (non-Javadoc)
     * @see com.projectbarbel.histo.model.Updater#effectiveFrom(java.time.LocalDate)
     */
    @Override
    public Updater effectiveFrom(LocalDate newEffectiveDate) {
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
    
    /* (non-Javadoc)
     * @see com.projectbarbel.histo.model.Updater#execute()
     */
    @Override
    public Updater execute() {
        BitemporalStamp newPrecedingStamp = BitemporalStamp.create(activity, oldVersion.getDocumentId(),
                EffectivePeriod.create().from(oldVersion.getEffectiveFromInstant()).until(newEffectiveDate),
                RecordPeriod.create(createdBy));
        BitemporalStamp newSubsequentStamp = BitemporalStamp.create(activity, oldVersion.getDocumentId(),
                EffectivePeriod.create().from(newEffectiveDate).until(oldVersion.getEffectiveUntilInstant()),
                RecordPeriod.create(createdBy));
        newPrecedingVersion = copier.apply(oldVersion, newPrecedingStamp);
        newSubsequentVersion = copier.apply(oldVersion, newSubsequentStamp);
        return this;
    }
    
    /* (non-Javadoc)
     * @see com.projectbarbel.histo.model.Updater#precedingVersion()
     */
    @Override
    public Bitemporal<?> precedingVersion() {
        return newPrecedingVersion;
    }

    /* (non-Javadoc)
     * @see com.projectbarbel.histo.model.Updater#subsequentVersion()
     */
    @Override
    public Bitemporal<?> subsequentVersion() {
        return newSubsequentVersion;
    }

}
