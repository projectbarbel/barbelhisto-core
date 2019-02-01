package com.projectbarbel.histo.persistence.api;

import java.io.Serializable;

import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;

public class BitemporalStamp {
    protected Serializable versionId;
    protected String documentId;
    protected String activity;
    protected EffectivePeriod effectiveTime;
    protected RecordPeriod recordTime;
    public Serializable getVersionId() {
        return versionId;
    }
    public void setVersionId(Serializable versionId) {
        this.versionId = versionId;
    }
    public String getDocumentId() {
        return documentId;
    }
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    public String getActivity() {
        return activity;
    }
    public void setActivity(String activity) {
        this.activity = activity;
    }
    public EffectivePeriod getEffectiveTime() {
        return effectiveTime;
    }
    public void setEffectiveTime(EffectivePeriod effectiveTime) {
        this.effectiveTime = effectiveTime;
    }
    public RecordPeriod getRecordTime() {
        return recordTime;
    }
    public void setRecordTime(RecordPeriod recordTime) {
        this.recordTime = recordTime;
    }
}
