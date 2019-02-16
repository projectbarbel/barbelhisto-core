package org.projectbarbel.histo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;
import org.projectbarbel.histo.BarbelHistoCore.UpdateLogRecord;
import org.projectbarbel.histo.functions.DefaultIDGenerator;
import org.projectbarbel.histo.functions.DefaultJournalUpdateStrategy;
import org.projectbarbel.histo.functions.DefaultPojoCopier;
import org.projectbarbel.histo.functions.DefaultProxyingFunction;
import org.projectbarbel.histo.functions.SimpleGsonPojoSerializer;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;

import com.google.gson.Gson;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.persistence.Persistence;
import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.persistence.offheap.OffHeapPersistence;
import com.googlecode.cqengine.persistence.onheap.OnHeapPersistence;
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
	private BarbelMode mode = BarbelHistoContext.getDefaultBarbelMode();
	private BiFunction<Object, BitemporalStamp, Object> pojoProxyingFunction = BarbelHistoContext
			.getDefaultProxyingFunction();
	private Function<Object, Object> pojoCopyFunction = BarbelHistoContext.getDefaultCopyFunction();
	private Supplier<Object> versionIdGenerator = BarbelHistoContext.getDefaultVersionIDGenerator();
	private Supplier<Object> documentIdGenerator = BarbelHistoContext.getDefaultDocumentIDGenerator();
	private Object backboneSupplier = BarbelHistoContext.getDefaultBackbone();
	private String activity = BarbelHistoContext.getDefaultActivity();
	private String user = BarbelHistoContext.getDefaultUser();
	private Map<Object, DocumentJournal> journalStore = new ConcurrentHashMap<Object, DocumentJournal>();
	private Gson gson = BarbelHistoContext.getDefaultGson();
	private IndexedCollection<UpdateLogRecord> updateLog = BarbelHistoContext.getDefaultUpdateLog();
	private Function<List<Bitemporal>, String> prettyPrinter = BarbelHistoContext.getDefaultPrettyPrinter();

	// some more complex context types
	private Function<BarbelHistoContext, PojoSerializer<Bitemporal>> persistenceSerializerProducer = BarbelHistoContext
			.getDefaultPersistenceSerializerProducer();
	private Function<BarbelHistoContext, BiConsumer<DocumentJournal, Bitemporal>> journalUpdateStrategyProducer = (
			context) -> new DefaultJournalUpdateStrategy(this);

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
	public BarbelMode getMode() {
		return mode;
	}

	/**
	 * Set the {@link BarbelMode} of this {@link BarbelHisto} instance. Default is
	 * {@link BarbelMode#POJO}. See {@link BarbelHisto} for more details on modes.
	 * 
	 * @param mode the mode
	 * @return the builder again
	 */
	public BarbelHistoBuilder withMode(BarbelMode mode) {
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
	 * customized by clients. Default is {@link DefaultJournalUpdateStrategy}.
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
	public Function<Object, Object> getPojoCopyFunction() {
		return pojoCopyFunction;
	}

	/**
	 * Set a custom pojo copy function. Required if clients use specific pojos that
	 * cannot be copied by {@link DefaultPojoCopier}.
	 * 
	 * @param pojoCopyFunction the custom copy function
	 * @return the builder again
	 */
	public BarbelHistoBuilder withPojoCopyFunction(Function<Object, Object> pojoCopyFunction) {
		Validate.isTrue(pojoCopyFunction != null, NONULLS);
		this.pojoCopyFunction = pojoCopyFunction;
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
	public BiFunction<Object, BitemporalStamp, Object> getPojoProxyingFunction() {
		return pojoProxyingFunction;
	}

	/**
	 * Customize the proxying in {@link BarbelMode#POJO}. Default is
	 * {@link DefaultProxyingFunction}. Clients may want to use more specific
	 * proxying functions with their pojos.
	 * 
	 * @param proxyingFunction the custom proxying function
	 * @return the builder again
	 */
	public BarbelHistoBuilder withPojoProxyingFunction(BiFunction<Object, BitemporalStamp, Object> proxyingFunction) {
		Validate.isTrue(proxyingFunction != null, NONULLS);
		this.pojoProxyingFunction = proxyingFunction;
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
	 * "own" data. It's possible to share this collection across multiple instances
	 * of {@link BarbelHisto} to ensure that locking is performed across these
	 * different {@link BarbelHisto} instances.
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
	 * This collection should not be shared across multiple instances of
	 * {@link BarbelHisto}. If you use persistent collections store the
	 * {@link BarbelHisto} instance as singleton bean to your application. Multiple
	 * threads are allowed access the {@link BarbelHisto} instance.
	 * 
	 * @see <a href=
	 *      "https://github.com/npgall/cqengine">https://github.com/npgall/cqengine</a>
	 * @param backbone the collection for the backbone
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

	@Override
	public Supplier<Object> getDocumentIdGenerator() {
		return documentIdGenerator;
	}

	/**
	 * Client may want to implememt their own version id generator. Make sure it
	 * will be unique. Default is {@link DefaultIDGenerator}.
	 * 
	 * @param versionIdGenerator the custom version id generator
	 * @return the builder
	 */
	public BarbelHistoBuilder withVersionIdGenerator(Supplier<Object> versionIdGenerator) {
		Validate.isTrue(versionIdGenerator != null, NONULLS);
		this.versionIdGenerator = versionIdGenerator;
		return this;
	}

	/**
	 * Clients can add a custom document Id generator here. Default is
	 * {@link DefaultIDGenerator}. {@link BarbelMode#POJO} {@link BarbelHisto} will
	 * expact the client to set the document id before saving the object.
	 * 
	 * @param documentIdGenerator the custom document id generator
	 * @return the builder
	 */
	public BarbelHistoBuilder withDocumentIdGenerator(Supplier<Object> documentIdGenerator) {
		Validate.isTrue(documentIdGenerator != null, NONULLS);
		this.documentIdGenerator = documentIdGenerator;
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
	 * @param defaultActivity the activity to store in record entries
	 * @return the builder again
	 */
	public BarbelHistoBuilder withActivity(String activity) {
		Validate.isTrue(activity != null, NONULLS);
		this.activity = activity;
		return this;
	}

	/**
	 * The user stored when creating records. Adopt this to user names or the
	 * like.
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
	 * Default is {@link SimpleGsonPojoSerializer}.
	 * 
	 * @param persistenceSerializerProducer
	 * @return the builder again
	 */
	public BarbelHistoBuilder withPersistenceSerializerProducer(
			Function<BarbelHistoContext, PojoSerializer<Bitemporal>> persistenceSerializerProducer) {
		Validate.isTrue(persistenceSerializerProducer != null, NONULLS);
		this.persistenceSerializerProducer = persistenceSerializerProducer;
		return this;
	}

}