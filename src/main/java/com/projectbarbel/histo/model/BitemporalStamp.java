package com.projectbarbel.histo.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Supplier;

import com.projectbarbel.histo.BarbelHistoContext;

/**
 * All instants representing utc time stamps.
 * 
 * @author niklasschlimm
 *
 */
public final class BitemporalStamp {

    protected final Serializable versionId;
    protected final String documentId;
    protected final String activity;
    protected final EffectivePeriod effectiveTime;
    protected final RecordPeriod recordTime;
    protected final Supplier<Serializable> versionIdGenerator;
    protected final Supplier<Serializable> documentIdGenerator;

    private BitemporalStamp(Builder builder) {
        this.versionIdGenerator = builder.versionIdGenerator != null ? builder.versionIdGenerator
                : BarbelHistoContext.getDefaultVersionIDGenerator();
        this.documentIdGenerator = builder.documentIdGenerator != null ? builder.documentIdGenerator
                : BarbelHistoContext.getDefaultDocumentIDGenerator();
        ;
        this.versionId = builder.versionId != null ? builder.versionId : versionIdGenerator.get();
        this.documentId = builder.documentId != null ? builder.documentId : (String) documentIdGenerator.get();
        this.activity = builder.activity != null ? builder.activity : BarbelHistoContext.getDefaultActivity();
        this.effectiveTime = Objects.requireNonNull(builder.effectiveTime);
        this.recordTime = Objects.requireNonNull(builder.recordTime);
    }

    public static BitemporalStamp initial() {
        return builder().withActivity(BarbelHistoContext.getDefaultActivity())
                .withDocumentId((String) BarbelHistoContext.getDefaultDocumentIDGenerator().get())
                .withVersionId(BarbelHistoContext.getDefaultVersionIDGenerator().get())
                .withEffectiveTime(EffectivePeriod.builder().build()).withRecordTime(RecordPeriod.builder().build())
                .build();
    }

    public static BitemporalStamp of(String activity, String documentId, EffectivePeriod effectiveTime,
            RecordPeriod recordTime) {
        return builder().withActivity(activity).withDocumentId(documentId).withEffectiveTime(effectiveTime)
                .withRecordTime(recordTime).withVersionId(BarbelHistoContext.getDefaultVersionIDGenerator().get())
                .build();
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
        return builder().withActivity(activity).withDocumentId(documentId).withEffectiveTime(effectiveTime)
                .withRecordTime(recordTime.inactivate(inactivatedBy)).withVersionId(versionId).build();
    }

    public boolean isActive() {
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
     * 
     * @return created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link BitemporalStamp}.
     */
    public static final class Builder {
        private Serializable versionId;
        private String documentId;
        private String activity;
        private EffectivePeriod effectiveTime;
        private RecordPeriod recordTime;
        private Supplier<Serializable> versionIdGenerator;
        private Supplier<Serializable> documentIdGenerator;

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

        public Builder withVersionIdGenerator(Supplier<Serializable> versionIdGenerator) {
            this.versionIdGenerator = versionIdGenerator;
            return this;
        }

        public Builder withDocumentIdGenerator(Supplier<Serializable> documentIdGenerator) {
            this.documentIdGenerator = documentIdGenerator;
            return this;
        }

        public BitemporalStamp build() {
            return new BitemporalStamp(this);
        }
    }

}
