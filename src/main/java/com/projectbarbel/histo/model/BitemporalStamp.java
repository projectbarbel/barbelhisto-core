package com.projectbarbel.histo.model;

import java.io.Serializable;
import java.util.Objects;

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

    private BitemporalStamp(Builder builder) {
        this.versionId = builder.versionId != null ? builder.versionId
                : BarbelHistoContext.instance().versionIdGenerator().get();
        this.documentId = Objects.requireNonNull(builder.documentId);
        this.activity = builder.activity != null ? builder.activity : BarbelHistoContext.instance().defaultActivity();
        this.effectiveTime = Objects.requireNonNull(builder.effectiveTime);
        this.recordTime = Objects.requireNonNull(builder.recordTime);
    }

    public static BitemporalStamp initial() {
        return builder().withActivity(BarbelHistoContext.instance().defaultActivity())
                .withDocumentId(BarbelHistoContext.instance().documentIdGenerator().get())
                .withVersionId(BarbelHistoContext.instance().versionIdGenerator().get())
                .withEffectiveTime(EffectivePeriod.builder().build()).withRecordTime(RecordPeriod.builder().build())
                .build();
    }

    public static BitemporalStamp of(String activity, String documentId, EffectivePeriod effectiveTime,
            RecordPeriod recordTime) {
        return builder().withActivity(activity).withDocumentId(documentId).withEffectiveTime(effectiveTime)
                .withRecordTime(recordTime).withVersionId(BarbelHistoContext.instance().versionIdGenerator().get())
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

    public static Builder builder() {
        return new Builder();
    }

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
