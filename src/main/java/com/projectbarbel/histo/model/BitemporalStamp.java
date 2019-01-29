package com.projectbarbel.histo.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Generated;

import com.projectbarbel.histo.functions.DefaultIDGenerator;

/**
 * All instants representing utc time stamps.
 * 
 * @author niklasschlimm
 *
 */
public final class BitemporalStamp {

    public final static Instant NOT_INACTIVATED = Instant.ofEpochMilli(Long.MAX_VALUE);
    public final static String NOBODY = "NOBODY";

    protected final Serializable versionId;
    protected final String documentId;
    protected final String activity;
    protected final EffectivePeriod effectiveTime;
    protected final RecordPeriod recordTime;
    protected final Supplier<Serializable> idSupplier;

    @Generated("SparkTools")
    private BitemporalStamp(Builder builder) {
        this.versionId = builder.versionId;
        this.documentId = builder.documentId;
        this.activity = builder.activity;
        this.effectiveTime = builder.effectiveTime;
        this.recordTime = builder.recordTime;
        this.idSupplier = builder.idSupplier;
    }

    public static BitemporalStamp of(String activity, String documentId, EffectivePeriod effectivePeriod,
            RecordPeriod recordPeriod) {
        return BitemporalStamp.builder().withVersionId(DefaultIDGenerator.generateId()).withActivity(Objects.requireNonNull(activity)).withDocumentId(Objects.requireNonNull(documentId))
                .withEffectiveTime(Objects.requireNonNull(effectivePeriod)).withRecordTime(Objects.requireNonNull(recordPeriod)).build();
    }

    public Object getVersionId() {
        return versionId;
    }

    public EffectivePeriod getEffectiveTime() {
        return effectiveTime;
    }

    public RecordPeriod getRecordTime() {
        return recordTime;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getActivity() {
        return activity;
    }

    public BitemporalStamp inactivatedCopy(String inactivatedBy) {
        return of(documentId, activity, 
                EffectivePeriod.create().from(effectiveTime.getEffectiveFromLocalDate()).until(effectiveTime.getEffectiveUntilLocalDate()),
                RecordPeriod.create(recordTime.getCreatedBy(), recordTime.getCreatedAt()).inactivate(inactivatedBy));
    }

    public boolean isActive () {
        return recordTime.getState().equals(BitemporalObjectState.ACTIVE);
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof BitemporalStamp)) {
            return false;
        }
        BitemporalStamp abstractValueObject = (BitemporalStamp) o;
        return Objects.equals(documentId, abstractValueObject.getDocumentId())
                && Objects.equals(effectiveTime, abstractValueObject.getEffectiveTime())
                && Objects.equals(recordTime, abstractValueObject.getRecordTime())
                && Objects.equals(activity, abstractValueObject.getActivity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentId, effectiveTime, recordTime, activity);
    }

    @Override
    public String toString() {
        return "BitemporalStamp [documentId=" + documentId + ", activity=" + activity + ", effectiveTime="
                + effectiveTime + ", recordTime=" + recordTime + "]";
    }

    /**
     * Creates builder to build {@link BitemporalStamp}.
     * @return created builder
     */
    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link BitemporalStamp}.
     */
    @Generated("SparkTools")
    public static final class Builder {
        private Serializable versionId;
        private String documentId;
        private String activity;
        private EffectivePeriod effectiveTime;
        private RecordPeriod recordTime;
        private Supplier<Serializable> idSupplier;

        private Builder() {
        }

        public Builder withVersionId(Serializable versionId) {
            this.versionId = versionId;
            return this;
        }

        public Builder withDocumentId(String documentId) {
            this.documentId = documentId;
            return this;
        }

        public Builder withActivity(String activity) {
            this.activity = activity;
            return this;
        }

        public Builder withEffectiveTime(EffectivePeriod effectiveTime) {
            this.effectiveTime = effectiveTime;
            return this;
        }

        public Builder withRecordTime(RecordPeriod recordTime) {
            this.recordTime = recordTime;
            return this;
        }

        public Builder withIdSupplier(Supplier<Serializable> idSupplier) {
            this.idSupplier = idSupplier;
            return this;
        }

        public BitemporalStamp build() {
            return new BitemporalStamp(this);
        }
    }

}
