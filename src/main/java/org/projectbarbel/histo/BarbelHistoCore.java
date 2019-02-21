package org.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.descending;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.projectbarbel.histo.DocumentJournal.ProcessingState;
import org.projectbarbel.histo.functions.EmbeddingJournalUpdateStrategy.JournalUpdateCase;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.model.RecordPeriod;
import org.projectbarbel.histo.model.UpdateCaseAware;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import com.googlecode.cqengine.resultset.common.NoSuchObjectException;

/**
 * The core component of {@link BarbelHisto}. See {@link BarbelHisto} for
 * details.
 * 
 * @author Niklas Schlimm
 *
 * @param <T> the business object type to manage
 */
public final class BarbelHistoCore<T> implements BarbelHisto<T> {

	public static final ThreadLocal<BarbelHistoContext> CONSTRUCTION_CONTEXT = new ThreadLocal<>();

	private static final String NOTNULL = "all arguments must not be null here";

	private final BarbelHistoContext context;
	private final IndexedCollection<T> backbone;
	private final Map<Object, DocumentJournal> journals;
	private final IndexedCollection<UpdateLogRecord> updateLog;
	private static final Map<Object, Object> validTypes = new HashMap<>();
	private final BarbelMode mode;

	@SuppressWarnings("unchecked")
	protected BarbelHistoCore(BarbelHistoContext context) {
		CONSTRUCTION_CONTEXT.set(context);
		this.context = Objects.requireNonNull(context);
		this.mode = Objects.requireNonNull(context.getMode());
		this.backbone = Objects.requireNonNull((IndexedCollection<T>) context.getBackboneSupplier().get());
		this.journals = Objects.requireNonNull(context.getJournalStore());
		this.updateLog = Objects.requireNonNull(context.getUpdateLog());
		CONSTRUCTION_CONTEXT.remove();
	}

	@Override
	public boolean save(T newVersion, LocalDate from, LocalDate until) {
		Validate.noNullElements(Arrays.asList(newVersion, from, until), NOTNULL);
		Validate.notNull(newVersion, NOTNULL);
		Validate.isTrue(from.isBefore(until), "from date must be before until date");
		T maiden = mode.drawMaiden(context, newVersion);
		validTypes.computeIfAbsent(maiden.getClass(), k -> mode.validateManagedType(context, maiden));
		Object id = mode.drawDocumentId(maiden);
		BitemporalStamp stamp = BitemporalStamp.of(context.getActivity(), id, EffectivePeriod.of(from, until),
				RecordPeriod.createActive(context));
		Bitemporal newManagedBitemporal = mode.snapshotMaiden(context, maiden, stamp);
		BiConsumer<DocumentJournal, Bitemporal> updateStrategy = context.getJournalUpdateStrategyProducer()
				.apply(context);
		DocumentJournal journal = journals.computeIfAbsent(id,
				k -> DocumentJournal.create(ProcessingState.INTERNAL, context, backbone, k));
		if (journal.lockAcquired()) {
			updateStrategy.accept(journal, newManagedBitemporal);
		} else {
			throw new ConcurrentModificationException(
					"the journal for id=" + id.toString() + " is locked - try again later");
		}
		journal.unlock();
		updateLog.add(new UpdateLogRecord(journal.getLastInsert(), newManagedBitemporal,
				updateStrategy instanceof UpdateCaseAware ? ((UpdateCaseAware) updateStrategy).getActualCase() : null,
				context.getUser()));
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> retrieve(Query<T> query) {
		Validate.isTrue(query != null, NOTNULL);
		return doRetrieveList(() -> (List<T>) backbone.retrieve(query).stream()
				.map(o -> mode.copyManagedBitemporal(context, (Bitemporal) o)).collect(Collectors.toList()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> retrieve(Query<T> query, QueryOptions options) {
		Validate.isTrue(query != null && options != null, NOTNULL);
		return doRetrieveList(() -> (List<T>) backbone.retrieve(query, options).stream()
				.map(o -> mode.copyManagedBitemporal(context, (Bitemporal) o)).collect(Collectors.toList()));
	}

	@Override
	public String prettyPrintJournal(Object id) {
		Validate.isTrue(id != null, NOTNULL);
		if (journals.containsKey(id))
			return context.getPrettyPrinter().apply(journals.get(id).list());
		else
			return "";
	}

	public BarbelHistoContext getContext() {
		return context;
	}

	public UpdateLogRecord getLastUpdate() {
		return updateLog
				.retrieve(equal(UpdateLogRecord.USER_ATTRIBUTE, context.getUser()),
						queryOptions(orderBy(descending(UpdateLogRecord.TIMESTAMP))))
				.stream().findFirst()
				.orElseThrow(() -> new IllegalStateException("not update performed by this user yet"));
	}

	public DocumentJournal getDocumentJournal(Object id) {
		Validate.isTrue(id != null, NOTNULL);
		return journals.get(id);
	}

	public static class UpdateLogRecord {

		public static final Attribute<UpdateLogRecord, String> USER_ATTRIBUTE = new SimpleAttribute<UpdateLogRecord, String>(
				"user") {
			public String getValue(UpdateLogRecord logEntry, QueryOptions queryOptions) {
				return logEntry.user;
			}
		};

		public static final Attribute<UpdateLogRecord, ChronoZonedDateTime<LocalDate>> TIMESTAMP = new SimpleAttribute<UpdateLogRecord, ChronoZonedDateTime<LocalDate>>(
				"timestamp") {
			public ZonedDateTime getValue(UpdateLogRecord logEntry, QueryOptions queryOptions) {
				return logEntry.createdAt;
			}
		};

		public final ZonedDateTime createdAt;
		public final List<Bitemporal> newVersions;
		public final Bitemporal requestedUpdate;
		public final JournalUpdateCase updateCase;
		public final String user;

		public UpdateLogRecord(List<Bitemporal> newVersions, Bitemporal requestedUpdate, JournalUpdateCase updateCase,
				String user) {
			super();
			this.newVersions = newVersions;
			this.requestedUpdate = requestedUpdate;
			this.updateCase = updateCase;
			this.createdAt = ZonedDateTime.now();
			this.user = user;
		}
	}

	@Override
	public void populate(Collection<Bitemporal> bitemporals) {
		Validate.isTrue(bitemporals != null, "bitemporals cannot be null");
		Validate.validState(backbone.isEmpty(), "backbone must be empty when calling populate");
		backbone.addAll(mode.customPersistenceObjectsToManagedBitemporals(context, bitemporals));
	}

	@Override
	public Collection<Bitemporal> dump(DumpMode dumpMode) {
		Validate.isTrue(dumpMode != null, NOTNULL);
		Collection<Bitemporal> collection = mode.managedBitemporalToCustomPersistenceObjects(backbone);
		if (dumpMode.equals(DumpMode.CLEARCOLLECTION)) {
			backbone.clear();
			journals.clear();
			updateLog.clear();
		}
		return collection;
	}

	public Collection<UpdateLogRecord> getUpdateLog() {
		return updateLog;
	}

	@Override
	public DocumentJournal timeshift(Object id, LocalDateTime time) {
		Validate.isTrue(id != null && time != null, NOTNULL);
		Validate.isTrue(
				time.isBefore(BarbelHistoContext.getBarbelClock().now().toLocalDateTime())
						|| time.equals(BarbelHistoContext.getBarbelClock().now().toLocalDateTime()),
				"timeshift only allowed in the past");
		ResultSet<T> result = backbone.retrieve(BarbelQueries.journalAt(id, time));
		IndexedCollection<Bitemporal> copiedAndActivatedBitemporals = result.stream()
				.map(d -> mode.copyManagedBitemporal(context, (Bitemporal) d))
				.collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
		copiedAndActivatedBitemporals.forEach(d -> d.getBitemporalStamp().getRecordTime().activate());
		return DocumentJournal.create(ProcessingState.EXTERNAL, context, copiedAndActivatedBitemporals, id);
	}

	private List<T> doRetrieveList(Supplier<List<T>> retrieveOperation) {
		try {
			return retrieveOperation.get();
		} catch (ClassCastException e) {
			if (e.getMessage().contains("Bitemporal"))
				throw new ClassCastException(
						"a ClassCastException was thrown on retrieval of items - maybe using persistence and forgot to add @PersistenceConfig(serializer=BarbelPojoSerializer.class) to the pojo?");
			throw e;
		}
	}

	public int size() {
		return backbone.size();
	}

	public enum DumpMode {
		CLEARCOLLECTION, READONLY;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<T> retrieveOne(Query<T> query) {
		try {
			T object = (T) mode.copyManagedBitemporal(context, (Bitemporal) backbone.retrieve(query).uniqueResult());
			return Optional.of(object);
		} catch (NoSuchObjectException e) {
			return Optional.empty();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<T> retrieveOne(Query<T> query, QueryOptions options) {
		try {
			T object = (T) mode.copyManagedBitemporal(context,
					(Bitemporal) backbone.retrieve(query, options).uniqueResult());
			return Optional.of(object);
		} catch (NoSuchObjectException e) {
			return Optional.empty();
		}
	}

}
