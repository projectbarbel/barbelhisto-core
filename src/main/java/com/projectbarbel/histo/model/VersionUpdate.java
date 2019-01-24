package com.projectbarbel.histo.model;

import java.time.LocalDate;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.BarbelHistoHelper;

public class VersionUpdate<O> {
    private Bitemporal<O> precedingVersion;
    private Bitemporal<O> subsequentVersion;

    public static <T, O extends Bitemporal<T>> VersionUpdate<O> of(O currentVersion, LocalDate withEffectiveDate,
            String activity, String createdBy) {
        Validate.inclusiveBetween(currentVersion.getEffectiveFromInstant().toEpochMilli(),
                currentVersion.getEffectiveUntilInstant().toEpochMilli(),
                BarbelHistoHelper.effectiveDateToEffectiveUTCInstant(withEffectiveDate).toEpochMilli(),
                "effective date of new version must be withing effective period of current version");
        BitemporalStamp precedingStamp = BitemporalStamp.create(activity, currentVersion.getDocumentId(),
                EffectivePeriod.create().from(currentVersion.getEffectiveFrom()).until(withEffectiveDate),
                RecordPeriod.create(createdBy));
        BitemporalStamp subsequentStamp = BitemporalStamp.create(activity, currentVersion.getDocumentId(),
                EffectivePeriod.create().from(withEffectiveDate).until(currentVersion.getEffectiveUntil()),
                RecordPeriod.create(createdBy));
        return null;
    }

    public Bitemporal<O> precedingVersion() {
        return precedingVersion;
    }

    public Bitemporal<O> subsequentVersion() {
        return subsequentVersion;
    }

}
