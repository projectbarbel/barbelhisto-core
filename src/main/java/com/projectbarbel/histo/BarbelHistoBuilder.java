package com.projectbarbel.histo;

import java.util.function.Supplier;

public class BarbelHistoBuilder implements BarbelHistoContext {

    private String defaultActivity = "SYSTEMACTIVITY";
    private String defaultCreatedBy = "SYSTEM";
    private Supplier<?> versionIdGenerator;
    private Supplier<String> documentIdGenerator;

    public static BarbelHistoBuilder barbel() {
        return new BarbelHistoBuilder();
    }
    
    protected BarbelHistoBuilder() {
    }
    
    public BarbelHisto build() {
        return new BarbelHistoCore(this);
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

    public void withDefaultActivity(String defaultActivity) {
        this.defaultActivity = defaultActivity;
    }

    @Override
    public Supplier<?> getVersionIdGenerator() {
        return versionIdGenerator;
    }

    @Override
    public Supplier<String> getDocumentIdGenerator() {
        return documentIdGenerator;
    }

    public void withVersionIdGenerator(Supplier<?> versionIdGenerator) {
        this.versionIdGenerator = versionIdGenerator;
    }

    public void withDocumentIdGenerator(Supplier<String> documentIdGenerator) {
        this.documentIdGenerator = documentIdGenerator;
    }

}
