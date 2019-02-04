package com.projectbarbel.histo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.model.BitemporalVersion;

public class BarbelHistoBuilder implements BarbelHistoContext {

    private String defaultActivity = BarbelHistoContext.getDefaultActivity();
    private Supplier<?> versionIdGenerator = BarbelHistoContext.getDefaultVersionIDGenerator();
    private Supplier<?> documentIdGenerator = BarbelHistoContext.getDefaultDocumentIDGenerator();
    private IndexedCollection<BitemporalVersion> backbone = new ConcurrentIndexedCollection<>();
    private String activity = BarbelHistoContext.getDefaultActivity();
    private String user = BarbelHistoContext.getDefaultUser();
    private Map<Object, DocumentJournal<BitemporalVersion>> journalStore = new ConcurrentHashMap<Object, DocumentJournal<BitemporalVersion>>();

    public static BarbelHistoBuilder barbel() {
        return new BarbelHistoBuilder();
    }

    protected BarbelHistoBuilder() {
    }

    public BarbelHisto build() {
        return new BarbelHistoCore(this);
    }

    public Map<Object, DocumentJournal<BitemporalVersion>> getJournalStore() {
        return journalStore;
    }

    public void withJournalStore(Map<Object, DocumentJournal<BitemporalVersion>> journalStore) {
        this.journalStore = journalStore;
    }

    @Override
    public IndexedCollection<BitemporalVersion> getBackbone() {
        return backbone;
    }

    public BarbelHistoBuilder withBackbone(IndexedCollection<BitemporalVersion> backbone) {
        this.backbone = backbone;
        return this;
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

    public void withUser(String user) {
        this.user = user;
    }
    @Override
    public String getUser() {
        return user;
    }
    
}
