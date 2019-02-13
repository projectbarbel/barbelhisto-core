package com.projectbarbel.histo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;

import com.google.gson.Gson;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.persistence.support.serialization.PojoSerializer;
import com.projectbarbel.histo.BarbelHistoCore.UpdateLogRecord;
import com.projectbarbel.histo.functions.DefaultJournalUpdateStrategy;
import com.projectbarbel.histo.functions.DefaultPojoCopier;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DocumentJournal;
import com.projectbarbel.histo.model.Systemclock;

public final class BarbelHistoBuilder implements BarbelHistoContext {

    private static final String NONULLS = "null values not allowed when building barbel context";
    private static PojoSerializer<Bitemporal> persistenceSerializerSingleton = BarbelHistoContext.getDefaultPersistenceSerializerSingleton();
    private static BiFunction<Object, BitemporalStamp, Object> persistencePojoProxyingFunction = BarbelHistoContext.getDefaultProxyingFunction();
    
    // simple context types
    private BarbelMode mode = BarbelHistoContext.getDefaultBarbelMode();
    private BiFunction<Object, BitemporalStamp, Object> pojoProxyingFunction = BarbelHistoContext.getDefaultProxyingFunction();
    private Function<Object, Object> pojoCopyFunction = BarbelHistoContext.getDefaultCopyFunction();
    private String defaultActivity = BarbelHistoContext.getDefaultActivity();
    private Supplier<Object> versionIdGenerator = BarbelHistoContext.getDefaultVersionIDGenerator();
    private Supplier<Object> documentIdGenerator = BarbelHistoContext.getDefaultDocumentIDGenerator();
    @SuppressWarnings("rawtypes")
    private IndexedCollection backbone = BarbelHistoContext.getDefaultBackbone();
    private String activity = BarbelHistoContext.getDefaultActivity();
    private String user = BarbelHistoContext.getDefaultUser();
    private Map<Object, DocumentJournal> journalStore = new ConcurrentHashMap<Object, DocumentJournal>();
    private Gson gson = BarbelHistoContext.getDefaultGson();
    private Systemclock clock = BarbelHistoContext.getDefaultClock();
    private IndexedCollection<UpdateLogRecord> updateLog = BarbelHistoContext.getDefaultUpdateLog();
    private Function<List<Bitemporal>, String> prettyPrinter = BarbelHistoContext.getDefaultPrettyPrinter(); 
    
    // some more complex context types
    private Function<BarbelHistoContext, BiConsumer<DocumentJournal, Bitemporal>> journalUpdateStrategyProducer = (
            context) -> new DefaultJournalUpdateStrategy(this);
    private BarbelHistoFactory barbelFactory = new BarbelHistoFactory(this);

    public static BarbelHistoBuilder barbel() {
        BarbelHistoBuilder builder = new BarbelHistoBuilder();
        return builder;
    }

    protected BarbelHistoBuilder() {
    }

    public <T> BarbelHisto<T> build() {
        if (pojoCopyFunction instanceof DefaultPojoCopier)
            ((DefaultPojoCopier) pojoCopyFunction).setGson(gson);
        return new BarbelHistoCore<T>(this);
    }

    @Override
    public Function<List<Bitemporal>, String> getPrettyPrinter() {
        return prettyPrinter;
    }

    public BarbelHistoBuilder withPrettyPrinter(Function<List<Bitemporal>, String> prettyPrinter) {
        Validate.isTrue(prettyPrinter!=null,NONULLS);
        this.prettyPrinter = prettyPrinter;
        return this;
    }

    @Override
    public IndexedCollection<UpdateLogRecord> getUpdateLog() {
        return updateLog;
    }

    public BarbelHistoBuilder withUpdateLog(IndexedCollection<UpdateLogRecord> updateLog) {
        Validate.isTrue(updateLog!=null,NONULLS);
        this.updateLog = updateLog;
        return this;
    }

    @Override
    public Systemclock getClock() {
        return clock;
    }

    public BarbelHistoBuilder withClock(Systemclock clock) {
        Validate.isTrue(clock!=null,NONULLS);
        this.clock = clock;
        return this;
    }

    @Override
    public BarbelMode getMode() {
        return mode;
    }

    public BarbelHistoBuilder withMode(BarbelMode mode) {
        Validate.isTrue(mode!=null,NONULLS);
        this.mode = mode;
        return this;
    }

    @Override
    public BarbelHistoFactory getBarbelFactory() {
        return barbelFactory;
    }

    public BarbelHistoBuilder withBarbelFactory(BarbelHistoFactory barbelFactory) {
        Validate.isTrue(barbelFactory!=null,NONULLS);
        this.barbelFactory = barbelFactory;
        return this;
    }

    @Override
    public Function<BarbelHistoContext, BiConsumer<DocumentJournal, Bitemporal>> getJournalUpdateStrategyProducer() {
        return journalUpdateStrategyProducer;
    }

    public BarbelHistoBuilder withJournalUpdateStrategyProducer(
            Function<BarbelHistoContext, BiConsumer<DocumentJournal, Bitemporal>> journalUpdateStrategy) {
        Validate.isTrue(journalUpdateStrategy!=null,NONULLS);
        this.journalUpdateStrategyProducer = journalUpdateStrategy;
        return this;
    }

    @Override
    public Function<Object, Object> getPojoCopyFunction() {
        return pojoCopyFunction;
    }

    public BarbelHistoBuilder withPojoCopyFunction(Function<Object, Object> pojoCopyFunction) {
        Validate.isTrue(pojoCopyFunction!=null,NONULLS);
        this.pojoCopyFunction = pojoCopyFunction;
        return this;
   }

    public Gson getGson() {
        return gson;
    }

    public BarbelHistoBuilder withGson(Gson gson) {
        Validate.isTrue(gson!=null,NONULLS);
        this.gson = gson;
        return this;
    }

    @Override
    public BiFunction<Object, BitemporalStamp, Object> getPojoProxyingFunction() {
        return pojoProxyingFunction;
    }

    public BarbelHistoBuilder withPojoProxyingFunction(BiFunction<Object, BitemporalStamp, Object> proxyingFunction) {
        Validate.isTrue(proxyingFunction!=null,NONULLS);
        this.pojoProxyingFunction = proxyingFunction;
        return this;
    }

    @Override
    public Map<Object, DocumentJournal> getJournalStore() {
        return journalStore;
    }

    public BarbelHistoBuilder withJournalStore(Map<Object, DocumentJournal> journalStore) {
        Validate.isTrue(journalStore!=null,NONULLS);
        this.journalStore = journalStore;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> IndexedCollection<T> getBackbone() {
        return backbone;
    }

    public <T> BarbelHistoBuilder withBackbone(IndexedCollection<T> backbone) {
        Validate.isTrue(backbone!=null,NONULLS);
        this.backbone = backbone;
        return this;
    }

    public String getDefaultActivity() {
        return defaultActivity;
    }

    public BarbelHistoBuilder withDefaultActivity(String defaultActivity) {
        Validate.isTrue(defaultActivity!=null,NONULLS);
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

    public BarbelHistoBuilder withVersionIdGenerator(Supplier<Object> versionIdGenerator) {
        Validate.isTrue(versionIdGenerator!=null,NONULLS);
        this.versionIdGenerator = versionIdGenerator;
        return this;
    }

    public BarbelHistoBuilder withDocumentIdGenerator(Supplier<Object> documentIdGenerator) {
        Validate.isTrue(documentIdGenerator!=null,NONULLS);
        this.documentIdGenerator = documentIdGenerator;
        return this;
    }

    @Override
    public String getActivity() {
        return activity;
    }

    public BarbelHistoBuilder withActivity(String activity) {
        Validate.isTrue(activity!=null,NONULLS);
        this.activity = activity;
        return this;
    }

    public BarbelHistoBuilder withUser(String user) {
        Validate.isTrue(user!=null,NONULLS);
        this.user = user;
        return this;
    }

    @Override
    public String getUser() {
        return user;
    }

    public static PojoSerializer<Bitemporal> getPersistenceSerializerSingleton() {
        return persistenceSerializerSingleton;
    }

    public static void setPersistenceSerializerSingleton(PojoSerializer<Bitemporal> persistenceSerializerSingleton) {
        BarbelHistoBuilder.persistenceSerializerSingleton = persistenceSerializerSingleton;
    }

    public static BiFunction<Object, BitemporalStamp, Object> getPersistencePojoProxyingFunction() {
        return persistencePojoProxyingFunction;
    }

    public static void setPersistencePojoProxyingFunction(BiFunction<Object, BitemporalStamp, Object> persistencePojoProxyingFunction) {
        BarbelHistoBuilder.persistencePojoProxyingFunction = persistencePojoProxyingFunction;
    }
    
}