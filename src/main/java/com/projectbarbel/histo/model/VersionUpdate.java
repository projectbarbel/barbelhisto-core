package com.projectbarbel.histo.model;

import java.time.LocalDate;
import java.util.function.BiFunction;

import javax.annotation.Generated;

import org.apache.commons.lang3.Validate;

import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelHistoFactory.FactoryType;
import com.projectbarbel.histo.BarbelHistoHelper;

public class VersionUpdate {
    private Bitemporal<?> precedingVersion;
    private Bitemporal<?> subsequentVersion;
    private final static BiFunction<Bitemporal<?>, BitemporalStamp, Bitemporal<?>> copier = BarbelHistoFactory
            .createProduct(FactoryType.POJOCOPIER);

    @Generated("SparkTools")
    private VersionUpdate(Builder builder) {
        this.precedingVersion = builder.precedingVersion;
        this.subsequentVersion = builder.subsequentVersion;
    }

    public static VersionUpdate of(Bitemporal<?> currentVersion, LocalDate withEffectiveDate, String activity,
            String createdBy) {
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
        return VersionUpdate.builder()
                .withPrecedingVersion(copier.apply(currentVersion, precedingStamp))
                .withSubsequentVersion(copier.apply(currentVersion, subsequentStamp)).build();
    }

    public Bitemporal<?> precedingVersion() {
        return precedingVersion;
    }

    public Bitemporal<?> subsequentVersion() {
        return subsequentVersion;
    }

    /**
     * Creates builder to build {@link VersionUpdate}.
     * 
     * @return created builder
     */
    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link VersionUpdate}.
     */
    @Generated("SparkTools")
    public static final class Builder {
        private Bitemporal<?> precedingVersion;
        private Bitemporal<?> subsequentVersion;

        private Builder() {
        }

        public Builder withPrecedingVersion(Bitemporal<?> precedingVersion) {
            this.precedingVersion = precedingVersion;
            return this;
        }

        public Builder withSubsequentVersion(Bitemporal<?> subsequentVersion) {
            this.subsequentVersion = subsequentVersion;
            return this;
        }

        public VersionUpdate build() {
            return new VersionUpdate(this);
        }
    }

}
