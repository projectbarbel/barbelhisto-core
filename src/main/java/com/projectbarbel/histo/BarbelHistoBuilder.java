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

public final class BarbelHistoBuilder<T> implements BarbelHistoContext<T> {

    // simple context types
    private BarbelMode mode = BarbelHistoContext.getDefaultBarbelMode();
    private BiFunction<T, BitemporalStamp, T> pojoProxyingFunction = BarbelHistoContext.getDefaultProxyingFunction();
    private Function<T, T> pojoCopyFunction = BarbelHistoContext.getDefaultCopyFunction();
    private String defaultActivity = BarbelHistoContext.getDefaultActivity();
    private Supplier<?> versionIdGenerator = BarbelHistoContext.getDefaultVersionIDGenerator();
    private Supplier<?> documentIdGenerator = BarbelHistoContext.getDefaultDocumentIDGenerator();
    private IndexedCollection<T> backbone = new ConcurrentIndexedCollection<>();
    private String activity = BarbelHistoContext.getDefaultActivity();
    private String user = BarbelHistoContext.getDefaultUser();
    private Map<Object, DocumentJournal<T>> journalStore = new ConcurrentHashMap<Object, DocumentJournal<T>>();
    private Gson gson = BarbelHistoContext.getDefaultGson();
    private Function<UpdateExecutionContext<T>, VersionUpdateResult<T>> versionUpdateExecutionStrategy = BarbelHistoContext
            .getDefaultVersionUpdateExecutionStrategy();
    // some more complex context types
    private Function<BarbelHistoContext<T>, BiFunction<DocumentJournal<T>, VersionUpdateResult<T>, List<T>>> journalUpdateStrategy = (
            context) -> new JournalUpdateStrategyEmbedding<T>(this);
    private BarbelHistoFactory<T> barbelFactory;

    public static <T> BarbelHistoBuilder<T> barbel() {
        BarbelHistoBuilder<T> builder = new BarbelHistoBuilder<T>();
        builder.withBarbelFactory(new BarbelHistoFactory<T>(builder));
        return builder;
    }

    protected BarbelHistoBuilder() {
    }

    @SuppressWarnings("unchecked")
    public <O> BarbelHisto<O> build() {
        if (pojoCopyFunction instanceof GsonPojoCopier)
            ((GsonPojoCopier<T>) pojoCopyFunction).setGson(gson);
        return new BarbelHistoCore<O>((BarbelHistoContext<O>) this);
    }

    @Override
    public BarbelMode getMode() {
        return mode;
    }

    public BarbelHistoBuilder<T> withMode(BarbelMode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public BarbelHistoFactory<T> getBarbelFactory() {
        return barbelFactory;
    }

    public void withBarbelFactory(BarbelHistoFactory<T> barbelFactory) {
        this.barbelFactory = barbelFactory;
    }

    @Override
    public Function<UpdateExecutionContext<T>, VersionUpdateResult<T>> getVersionUpdateExecutionStrategy() {
        return versionUpdateExecutionStrategy;
    }

    public void withVersionUpdateExecutionStrategy(
            Function<UpdateExecutionContext<T>, VersionUpdateResult<T>> versionUpdateExecutionStrategy) {
        this.versionUpdateExecutionStrategy = versionUpdateExecutionStrategy;
    }

    @Override
    public Function<BarbelHistoContext<T>, BiFunction<DocumentJournal<T>, VersionUpdateResult<T>, List<T>>> getJournalUpdateStrategy() {
        return journalUpdateStrategy;
    }

    public void withJournalUpdateStrategy(
            Function<BarbelHistoContext<T>, BiFunction<DocumentJournal<T>, VersionUpdateResult<T>, List<T>>> journalUpdateStrategy) {
        this.journalUpdateStrategy = journalUpdateStrategy;
    }

    @Override
    public Function<T, T> getPojoCopyFunction() {
        return pojoCopyFunction;
    }

    public void withPojoCopyFunction(Function<T, T> pojoCopyFunction) {
        this.pojoCopyFunction = pojoCopyFunction;
    }

    public Gson getGson() {
        return gson;
    }

    public void withGson(Gson gson) {
        this.gson = gson;
    }

    @Override
    public BiFunction<T, BitemporalStamp, T> getPojoProxyingFunction() {
        return pojoProxyingFunction;
    }

    public void withPojoProxyingFunction(BiFunction<T, BitemporalStamp, T> proxyingFunction) {
        this.pojoProxyingFunction = proxyingFunction;
    }

    @Override
    public Map<Object, DocumentJournal<T>> getJournalStore() {
        return journalStore;
    }

    public void withJournalStore(Map<Object, DocumentJournal<T>> journalStore) {
        this.journalStore = journalStore;
    }

    @Override
    public IndexedCollection<T> getBackbone() {
        return backbone;
    }

    public BarbelHistoContext<T> withBackbone(IndexedCollection<T> backbone) {
        this.backbone = backbone;
        return this;
    }

    public String getDefaultActivity() {
        return defaultActivity;
    }

    public BarbelHistoContext<T> withDefaultActivity(String defaultActivity) {
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

    public BarbelHistoContext<T> withVersionIdGenerator(Supplier<?> versionIdGenerator) {
        this.versionIdGenerator = versionIdGenerator;
        return this;
    }

    public BarbelHistoContext<T> withDocumentIdGenerator(Supplier<String> documentIdGenerator) {
        this.documentIdGenerator = documentIdGenerator;
        return this;
    }

    @Override
    public String getActivity() {
        return activity;
    }

    public BarbelHistoContext<T> withActivity(String activity) {
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