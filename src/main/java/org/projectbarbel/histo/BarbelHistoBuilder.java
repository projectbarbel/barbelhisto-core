package org.projectbarbel.histo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;
import org.projectbarbel.histo.BarbelHistoCore.UpdateLogRecord;
import org.projectbarbel.histo.functions.AdaptingKryoSerializer;
import org.projectbarbel.histo.functions.BarbelPojoSerializer;
import org.projectbarbel.histo.functions.CachingCGLibProxyingFunction;
import org.projectbarbel.histo.functions.EmbeddingJournalUpdateStrategy;
import org.projectbarbel.histo.functions.RitsClonerCopyFunction;
import org.projectbarbel.histo.functions.SimpleGsonPojoCopier;
import org.projectbarbel.histo.functions.UUIDGenerator;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;

import com.google.gson.Gson;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.persistence.Persistence;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.persistence.offheap.OffHeapPersistence;
import com.googlecode.cqengine.persistence.onheap.OnHeapPersistence;
import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;
import com.googlecode.cqengine.persistence.support.serialization.PojoSerializer;

/**
 * Builder class for {@link BarbelHisto}. Use
 * <code>BarbelHistoBuilder.barbel().build()</code> to receive the default
 * implementation. Allows to set a variety of context objects to adopt the
 * behaviour of {@link BarbelHisto} if required. Defaults are defined in
 * {@link BarbelHistoContext}.
 * 
 * @author Niklas Schlimm
 *
 */
public final class BarbelHistoBuilder implements BarbelHistoContext {

    private static final String NONULLS = "null values not allowed when building barbel context";

    // simple context types
    private AbstractBarbelMode mode = BarbelHistoContext.getDefaultBarbelMode();
    private Supplier<BiFunction<Object, BitemporalStamp, Object>> pojoProxyingFunctionSupplier = BarbelHistoContext
            .getDefaultProxyingFunctionSupplier();
    private Supplier<Function<Object, Object>> pojoCopyFunctionSupplier = BarbelHistoContext
            .getDefaultCopyFunctionSupplier();
    private Supplier<Object> versionIdGenerator = BarbelHistoContext.getDefaultVersionIDGenerator();
    private Object backboneSupplier = BarbelHistoContext.getDefaultBackbone();
    private String activity = BarbelHistoContext.getDefaultActivity();
    private String user = BarbelHistoContext.getDefaultUser();
    private Map<Object, DocumentJournal> journalStore = new ConcurrentHashMap<>();
    private Gson gson = BarbelHistoContext.getDefaultGson();
    private IndexedCollection<UpdateLogRecord> updateLog = BarbelHistoContext.getDefaultUpdateLog();
    private Function<List<Bitemporal>, String> prettyPrinter = BarbelHistoContext.getDefaultPrettyPrinter();
    private Map<String, Object> contextOptions = new HashMap<>();

    // some more complex context types
    private Function<BarbelHistoContext, PojoSerializer<Bitemporal>> persistenceSerializerProducer = BarbelHistoContext
            .getDefaultPersistenceSerializerProducer();
    private Function<BarbelHistoContext, BiConsumer<DocumentJournal, Bitemporal>> journalUpdateStrategyProducer = 
            context -> new EmbeddingJournalUpdateStrategy(this);

    public static BarbelHistoBuilder barbel() {
        return new BarbelHistoBuilder();
    }

    protected BarbelHistoBuilder() {
    }

    public <T> BarbelHisto<T> build() {
        if (pojoCopyFunctionSupplier instanceof SimpleGsonPojoCopier)
            ((SimpleGsonPojoCopier) pojoCopyFunctionSupplier).setGson(gson);
        return new BarbelHistoCore<>(this);
    }

    public Map<String, Object> getContextOptions() {
        return contextOptions;
    }

    /**
     * Allows to pass custom options to custom implementations of functions.
     * 
     * @param contextOptions the options map
     * @return the builder
     */
    public BarbelHistoBuilder withContextOptions(Map<String, Object> contextOptions) {
        Validate.isTrue(contextOptions != null, NONULLS);
        this.contextOptions = contextOptions;
        return this;
    }

    @Override
    public Function<List<Bitemporal>, String> getPrettyPrinter() {
        return prettyPrinter;
    }

    /**
     * Register custom pretty printer for {@link DocumentJournal}s.
     * 
     * @param prettyPrinter the custom printer
     * @return the builder again
     */
    public BarbelHistoBuilder withPrettyPrinter(Function<List<Bitemporal>, String> prettyPrinter) {
        Validate.isTrue(prettyPrinter != null, NONULLS);
        this.prettyPrinter = prettyPrinter;
        return this;
    }

    @Override
    public IndexedCollection<UpdateLogRecord> getUpdateLog() {
        return updateLog;
    }

    /**
     * Register a custom update log collection, maybe persistent. See
     * {@link Persistence} and its implementation {@link DiskPersistence} and
     * {@link OffHeapPersistence} for options.
     * 
     * @param updateLog the custom log
     * @return the builder again
     */
    public BarbelHistoBuilder withUpdateLog(IndexedCollection<UpdateLogRecord> updateLog) {
        Validate.isTrue(updateLog != null, NONULLS);
        this.updateLog = updateLog;
        return this;
    }

    @Override
    public AbstractBarbelMode getMode() {
        return mode;
    }

    /**
     * Set the {@link AbstractBarbelMode} of this {@link BarbelHisto} instance. Default is
     * {@link AbstractBarbelMode#POJO}. See {@link BarbelHisto} for more details on modes.
     * 
     * @param mode the mode
     * @return the builder again
     */
    public BarbelHistoBuilder withMode(AbstractBarbelMode mode) {
        Validate.isTrue(mode != null, NONULLS);
        this.mode = mode;
        return this;
    }

    @Override
    public Function<BarbelHistoContext, BiConsumer<DocumentJournal, Bitemporal>> getJournalUpdateStrategyProducer() {
        return journalUpdateStrategyProducer;
    }

    /**
     * Set the strategy how to update a journal. Core functionality usually not
     * customized by clients. Default is {@link EmbeddingJournalUpdateStrategy}.
     * 
     * @param journalUpdateStrategy the custom strategy
     * @return the builder again
     */
    public BarbelHistoBuilder withJournalUpdateStrategyProducer(
            Function<BarbelHistoContext, BiConsumer<DocumentJournal, Bitemporal>> journalUpdateStrategy) {
        Validate.isTrue(journalUpdateStrategy != null, NONULLS);
        this.journalUpdateStrategyProducer = journalUpdateStrategy;
        return this;
    }

    @Override
    public Supplier<Function<Object, Object>> getPojoCopyFunctionSupplier() {
        return pojoCopyFunctionSupplier;
    }

    /**
     * Set a custom POJO copy function. Required if clients use specific POJOs that
     * cannot be copied by the default {@link RitsClonerCopyFunction}.
     * 
     * @param pojoCopyFunction the custom copy function
     * @return the builder again
     */
    public BarbelHistoBuilder withPojoCopyFunctionSupplier(Supplier<Function<Object, Object>> pojoCopyFunction) {
        Validate.isTrue(pojoCopyFunction != null, NONULLS);
        this.pojoCopyFunctionSupplier = pojoCopyFunction;
        return this;
    }

    public Gson getGson() {
        return gson;
    }

    /**
     * Define a custom {@link Gson} for use with {@link BarbelHisto}. Clients may
     * have specific requirements here.
     * 
     * @param gson the custom {@link Gson}
     * @return the builder again
     */
    public BarbelHistoBuilder withGson(Gson gson) {
        Validate.isTrue(gson != null, NONULLS);
        this.gson = gson;
        return this;
    }

    @Override
    public Supplier<BiFunction<Object, BitemporalStamp, Object>> getPojoProxyingFunctionSupplier() {
        return pojoProxyingFunctionSupplier;
    }

    /**
     * Customize the proxying in {@link AbstractBarbelMode#POJO}. Default is
     * {@link CachingCGLibProxyingFunction}. Clients may want to use more specific
     * proxying functions with their POJOs.
     * 
     * @param proxyingFunction the custom proxying function
     * @return the builder again
     */
    public BarbelHistoBuilder withPojoProxyingFunctionSupplier(
            Supplier<BiFunction<Object, BitemporalStamp, Object>> proxyingFunction) {
        Validate.isTrue(proxyingFunction != null, NONULLS);
        this.pojoProxyingFunctionSupplier = proxyingFunction;
        return this;
    }

    @Override
    public Map<Object, DocumentJournal> getJournalStore() {
        return journalStore;
    }

    /**
     * Define the collection that stores {@link DocumentJournal} instances. Note
     * that {@link DocumentJournal} always works on the backbone collection set in
     * {@link BarbelHistoBuilder#withBackboneSupplier(Supplier)} and will never
     * "own" data.
     * 
     * @param journalStore the journal store collection
     * @return the builder again
     */
    public BarbelHistoBuilder withJournalStore(Map<Object, DocumentJournal> journalStore) {
        Validate.isTrue(journalStore != null, NONULLS);
        this.journalStore = journalStore;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Supplier<IndexedCollection<T>> getBackboneSupplier() {
        return (Supplier<IndexedCollection<T>>) backboneSupplier;
    }

    /**
     * The backbone collection of {@link BarbelHisto}. This collection actually
     * contains the complete version data. Objects stored here are never exposed
     * directly to clients, only copies are exposed. Adopt that collection to any
     * {@link IndexedCollection} of cqengine. See {@link Persistence}, i.e.
     * {@link DiskPersistence} and {@link OffHeapPersistence} for flavors of
     * different persistence options here. Default ist
     * {@link ConcurrentIndexedCollection} using {@link OnHeapPersistence}. <br>
     * <br>
     * If clients decide to use {@link DiskPersistence} or
     * {@link OffHeapPersistence} they need to add the {@link PersistenceConfig}
     * annotation additionally to their business classes. <br>
     * <br>
     * 
     * <pre>
     * <code>@PersistenceConfig(serializer=BarbelPojoSerializer.class, polymorphic=true)</code>
     * </pre>
     * 
     * This is always required if you not use the {@link OnHeapPersistence} and
     * regardless of the de-facto serializer you choose in
     * {@link #withPersistenceSerializerProducer(Function)}. The
     * {@link BarbelPojoSerializer} will forward processing to the
     * {@link AdaptingKryoSerializer} by default. If you define a different target
     * serializer, {@link BarbelPojoSerializer} will be forward requests to that
     * custom serializer.<br>
     * <br>
     * The backbone collection should not be shared across multiple instances of
     * {@link BarbelHisto}. If you use persistent collections, store the
     * {@link BarbelHisto} instance as singleton bean to your application. Multiple
     * threads are allowed access that {@link BarbelHisto} instance.
     * 
     * @see <a href=
     *      "https://github.com/npgall/cqengine">https://github.com/npgall/cqengine</a>
     * @param backbone the collection for the backbone
     * @param          <T> the type to manage
     * @return the builder again
     */
    public <T> BarbelHistoBuilder withBackboneSupplier(Supplier<IndexedCollection<T>> backbone) {
        Validate.isTrue(backbone != null, NONULLS);
        this.backboneSupplier = backbone;
        return this;
    }

    @Override
    public Supplier<Object> getVersionIdGenerator() {
        return versionIdGenerator;
    }

    /**
     * Client may want to implememt their own version id generator. Make sure it
     * will be unique. Default is {@link UUIDGenerator}.
     * 
     * @param versionIdGenerator the custom version id generator
     * @return the builder
     */
    public BarbelHistoBuilder withVersionIdGenerator(Supplier<Object> versionIdGenerator) {
        Validate.isTrue(versionIdGenerator != null, NONULLS);
        this.versionIdGenerator = versionIdGenerator;
        return this;
    }

    @Override
    public String getActivity() {
        return activity;
    }

    /**
     * The activity stored when creating records. Adopt this to process names or the
     * like.
     * 
     * @param activity the activity to store in record entries
     * @return the builder again
     */
    public BarbelHistoBuilder withActivity(String activity) {
        Validate.isTrue(activity != null, NONULLS);
        this.activity = activity;
        return this;
    }

    /**
     * The user stored when creating records. Adopt this to user names or the like.
     * 
     * @param user the user to store in record entries
     * @return the builder again
     */
    public BarbelHistoBuilder withUser(String user) {
        Validate.isTrue(user != null, NONULLS);
        this.user = user;
        return this;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public Function<BarbelHistoContext, PojoSerializer<Bitemporal>> getPersistenceSerializerProducer() {
        return persistenceSerializerProducer;
    }

    /**
     * Clients may want to decide how data is serialized into persistent storage.
     * Default is {@link AdaptingKryoSerializer}. To validate if your POJO is
     * serializable call
     * {@link AdaptingKryoSerializer#validateObjectIsRoundTripSerializable(BarbelHistoContext, Object)}.
     * 
     * @param persistenceSerializerProducer the producer of the serializer
     * @return the builder again
     */
    public BarbelHistoBuilder withPersistenceSerializerProducer(
            Function<BarbelHistoContext, PojoSerializer<Bitemporal>> persistenceSerializerProducer) {
        Validate.isTrue(persistenceSerializerProducer != null, NONULLS);
        this.persistenceSerializerProducer = persistenceSerializerProducer;
        return this;
    }

}