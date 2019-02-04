package com.projectbarbel.histo;

import java.util.function.Supplier;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.model.BitemporalVersion;

public class BarbelHistoBuilder implements BarbelHistoContext {

    private String defaultActivity = BarbelHistoContext.getDefaultActivity();
    private String defaultCreatedBy = BarbelHistoContext.getDefaultCreatedBy();
    private Supplier<?> versionIdGenerator = BarbelHistoContext.getDefaultVersionIDGenerator();
    private Supplier<?> documentIdGenerator = BarbelHistoContext.getDefaultDocumentIDGenerator();
    private IndexedCollection<BitemporalVersion> backbone = new ConcurrentIndexedCollection<>();
    private String activity = BarbelHistoContext.getDefaultActivity();

    public static BarbelHistoBuilder barbel() {
        return new BarbelHistoBuilder();
    }

    protected BarbelHistoBuilder() {
    }

    public BarbelHisto build() {
        return new BarbelHistoCore(this);
    }

    @Override
    public IndexedCollection<BitemporalVersion> getBackbone() {
        return backbone;
    }

    public BarbelHistoBuilder withBackbone(IndexedCollection<BitemporalVersion> backbone) {
        this.backbone = backbone;
        return this;
    }

    public String getDefaultCreatedBy() {
        return defaultCreatedBy;
    }

    public void withDefaultCreatedBy(String defaultCreatedBy) {
        this.defaultCreatedBy = defaultCreatedBy;
    }

    public String getDefaultActivity() {
        return defaultActivity;
    }

    public BarbelHistoBuilder withDefaultActivity(String defaultActivity) {
        this.defaultActivity = defaultActivity;
        return this;
    }

    @Override
    public Supplier<?> getVersionIdGenerator() {
        return versionIdGenerator;
    }

    @Override
    public Supplier<?> getDocumentIdGenerator() {
        return documentIdGenerator;
    }

    public BarbelHistoBuilder withVersionIdGenerator(Supplier<?> versionIdGenerator) {
        this.versionIdGenerator = versionIdGenerator;
        return this;
    }

    public BarbelHistoBuilder withDocumentIdGenerator(Supplier<String> documentIdGenerator) {
        this.documentIdGenerator = documentIdGenerator;
        return this;
    }

    @Override
    public String getActivity() {
        return activity;
    }

    public BarbelHistoBuilder withActivity(String activity) {
        this.activity = activity;
        return this;
    }
    
}
