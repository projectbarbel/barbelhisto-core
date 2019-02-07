package com.projectbarbel.histo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.google.gson.Gson;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.journal.VersionUpdate.UpdateExecutionContext;
import com.projectbarbel.histo.journal.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.journal.functions.GsonPojoCopier;
import com.projectbarbel.histo.journal.functions.JournalUpdateStrategyEmbedding;
import com.projectbarbel.histo.model.BitemporalStamp;

public final class BarbelHistoBuilder implements BarbelHistoContext {

    // simple context types
    private BarbelMode mode = BarbelHistoContext.getDefaultBarbelMode();
    private BiFunction<Object, BitemporalStamp, Object> pojoProxyingFunction = BarbelHistoContext.getDefaultProxyingFunction();
    private Function<Object, Object> pojoCopyFunction = BarbelHistoContext.getDefaultCopyFunction();
    private String defaultActivity = BarbelHistoContext.getDefaultActivity();
    private Supplier<Object> versionIdGenerator = BarbelHistoContext.getDefaultVersionIDGenerator();
    private Supplier<Object> documentIdGenerator = BarbelHistoContext.getDefaultDocumentIDGenerator();
    private IndexedCollection<Object> backbone = new ConcurrentIndexedCollection<>();
    private String activity = BarbelHistoContext.getDefaultActivity();
    private String user = BarbelHistoContext.getDefaultUser();
    private Map<Object, DocumentJournal> journalStore = new ConcurrentHashMap<Object, DocumentJournal>();
    private Gson gson = BarbelHistoContext.getDefaultGson();
    private Function<UpdateExecutionContext, VersionUpdateResult> versionUpdateExecutionStrategy = BarbelHistoContext
            .getDefaultVersionUpdateExecutionStrategy();
    // some more complex context types
    private Function<BarbelHistoContext, BiFunction<DocumentJournal, VersionUpdateResult, List<Object>>> journalUpdateStrategy = (
            context) -> new JournalUpdateStrategyEmbedding(this);
    private BarbelHistoFactory barbelFactory;

    public static BarbelHistoBuilder barbel() {
        BarbelHistoBuilder builder = new BarbelHistoBuilder();
        builder.withBarbelFactory(new BarbelHistoFactory(builder));
        return builder;
    }

    protected BarbelHistoBuilder() {
    }

    public  BarbelHisto build() {
        if (pojoCopyFunction instanceof GsonPojoCopier)
            ((GsonPojoCopier) pojoCopyFunction).setGson(gson);
        return new BarbelHistoCore(this);
    }

    @Override
    public BarbelMode getMode() {
        return mode;
    }

    public BarbelHistoBuilder withMode(BarbelMode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public BarbelHistoFactory getBarbelFactory() {
        return barbelFactory;
    }

    public void withBarbelFactory(BarbelHistoFactory barbelFactory) {
        this.barbelFactory = barbelFactory;
    }

    @Override
    public Function<UpdateExecutionContext, VersionUpdateResult> getVersionUpdateExecutionStrategy() {
        return versionUpdateExecutionStrategy;
    }

    public void withVersionUpdateExecutionStrategy(
            Function<UpdateExecutionContext, VersionUpdateResult> versionUpdateExecutionStrategy) {
        this.versionUpdateExecutionStrategy = versionUpdateExecutionStrategy;
    }

    @Override
    public Function<BarbelHistoContext, BiFunction<DocumentJournal, VersionUpdateResult, List<Object>>> getJournalUpdateStrategy() {
        return journalUpdateStrategy;
    }

    public void withJournalUpdateStrategy(
            Function<BarbelHistoContext, BiFunction<DocumentJournal, VersionUpdateResult, List<Object>>> journalUpdateStrategy) {
        this.journalUpdateStrategy = journalUpdateStrategy;
    }

    @Override
    public Function<Object, Object> getPojoCopyFunction() {
        return pojoCopyFunction;
    }

    public void withPojoCopyFunction(Function<Object, Object> pojoCopyFunction) {
        this.pojoCopyFunction = pojoCopyFunction;
    }

    public Gson getGson() {
        return gson;
    }

    public void withGson(Gson gson) {
        this.gson = gson;
    }

    @Override
    public BiFunction<Object, BitemporalStamp, Object> getPojoProxyingFunction() {
        return pojoProxyingFunction;
    }

    public void withPojoProxyingFunction(BiFunction<Object, BitemporalStamp, Object> proxyingFunction) {
        this.pojoProxyingFunction = proxyingFunction;
    }

    @Override
    public Map<Object, DocumentJournal> getJournalStore() {
        return journalStore;
    }

    public void withJournalStore(Map<Object, DocumentJournal> journalStore) {
        this.journalStore = journalStore;
    }

    @Override
    public IndexedCollection<Object> getBackbone() {
        return backbone;
    }

    public BarbelHistoContext withBackbone(IndexedCollection<Object> backbone) {
        this.backbone = backbone;
        return this;
    }

    public String getDefaultActivity() {
        return defaultActivity;
    }

    public BarbelHistoContext withDefaultActivity(String defaultActivity) {
        this.defaultActivity = defaultActivity;
        return this;
    }

    @Override
    public Supplier<Object> getVersionIdGenerator() {
        return versionIdGenerator;
    }

    @Override
    public Supplier<Object> getDocumentIdGenerator() {
        return documentIdGenerator;
    }

    public BarbelHistoContext withVersionIdGenerator(Supplier<Object> versionIdGenerator) {
        this.versionIdGenerator = versionIdGenerator;
        return this;
    }

    public BarbelHistoContext withDocumentIdGenerator(Supplier<Object> documentIdGenerator) {
        this.documentIdGenerator = documentIdGenerator;
        return this;
    }

    @Override
    public String getActivity() {
        return activity;
    }

    public BarbelHistoContext withActivity(String activity) {
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