package com.projectbarbel.histo.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Generated;

import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelHistoFactory.FactoryType;

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
    protected final static Supplier<Serializable> idSupplier = BarbelHistoFactory.createProduct(FactoryType.IDSUPPLIER);

    @Generated("SparkTools")
    private BitemporalStamp(Builder builder) {
        this.versionId = Objects.requireNonNull(builder.versionId);
        this.documentId = Objects.requireNonNull(builder.documentId);
        this.activity = Objects.requireNonNull(builder.activity);
        this.effectiveTime = Objects.requireNonNull(builder.effectiveTime);
        this.recordTime = Objects.requireNonNull(builder.recordTime);
    }
    
    public static BitemporalStamp create(String activity, String documentId, EffectivePeriod effectivePeriod,
            RecordPeriod recordPeriod) {
        return BitemporalStamp.builder().withVersionId(Objects.requireNonNull(idSupplier.get())).withActivity(Objects.requireNonNull(activity)).withDocumentId(Objects.requireNonNull(documentId))
                .withEffectiveTime(Objects.requireNonNull(effectivePeriod)).withRecordTime(Objects.requireNonNull(recordPeriod)).build();
    }

    public BitemporalStamp(BitemporalStamp template) {
        this(template.getDocumentId(), template.getActivity(), template.getEffectiveTime(), template.getRecordTime());
    }

    public BitemporalStamp(String documentId, String activity, EffectivePeriod effectiveTime, RecordPeriod recordTime) {
        super();
        this.versionId = Objects.requireNonNull(idSupplier.get());
        this.documentId = Objects.requireNonNull(documentId);
        this.activity = Objects.requireNonNull(activity);
        this.effectiveTime = Objects.requireNonNull(effectiveTime);
        this.recordTime = Objects.requireNonNull(recordTime);
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

    public BitemporalStamp inactivatedCopy(String inactivatedBy) {
        return create(documentId, activity, 
                EffectivePeriod.create().from(effectiveTime.from).until(effectiveTime.until),
                RecordPeriod.create(recordTime.createdBy, recordTime.createdAt).inactivate(inactivatedBy));
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

        public BitemporalStamp build() {
            return new BitemporalStamp(this);
        }
    }

}
