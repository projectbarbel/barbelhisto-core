package org.projectbarbel.histo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.projectbarbel.histo.DocumentJournal.ProcessingState;
import org.projectbarbel.histo.event.EventType;
import org.projectbarbel.histo.event.EventType.RetrieveDataEvent;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.model.RecordPeriod;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import com.googlecode.cqengine.resultset.common.NoSuchObjectException;
import com.googlecode.cqengine.resultset.common.NonUniqueObjectException;

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
    private static final Map<Object, Object> validTypes = new HashMap<>();
    private final BarbelMode mode;

    @SuppressWarnings("unchecked")
    protected BarbelHistoCore(BarbelHistoContext context) {
        CONSTRUCTION_CONTEXT.set(context);
        this.context = Objects.requireNonNull(context);
        this.mode = Objects.requireNonNull(context.getMode());
        this.backbone = Objects.requireNonNull((IndexedCollection<T>) context.getBackboneSupplier().get());
        ((BarbelHistoBuilder) context).setBackbone(backbone);
        this.journals = Objects.requireNonNull(context.getJournalStore());
        EventType.BARBELINITIALIZED.create().with(context).postBothWay(context);
        CONSTRUCTION_CONTEXT.remove();
    }

    @SuppressWarnings("unchecked")
    @Override
    public T save(T newVersion, LocalDate from, LocalDate until) {
        Validate.noNullElements(Arrays.asList(newVersion, from, until), NOTNULL);
        Validate.notNull(newVersion, NOTNULL);
        Validate.isTrue(from.isBefore(until), "from date must be before until date");
        T maiden = mode.drawMaiden(context, newVersion);
        validTypes.computeIfAbsent(maiden.getClass(), k -> mode.validateManagedType(context, maiden));
        Object id = mode.drawDocumentId(maiden);
        DocumentJournal journal = journals.computeIfAbsent(id,
                k -> DocumentJournal.create(ProcessingState.INTERNAL, context, k));
        EventType.INITIALIZEJOURNAL.create()
                .with(DocumentJournal.create(ProcessingState.EXTERNAL, context, id)).with(this).postBothWay(context);
        if (journal.lockAcquired()) {
            try {
                BitemporalStamp stamp = BitemporalStamp.of(context.getActivity(), id, EffectivePeriod.of(from, until),
                        RecordPeriod.createActive(context));
                Bitemporal newManagedBitemporal = mode.snapshotMaiden(context, maiden, stamp);
                BiConsumer<DocumentJournal, Bitemporal> updateStrategy = context.getJournalUpdateStrategyProducer()
                        .apply(context);
                try {
                    EventType.ACQUIRELOCK.create().with(journal).postSynchronous(context);
                    journal.setLastUpdateRequest(newManagedBitemporal);
                    updateStrategy.accept(journal, newManagedBitemporal);
                    return (T) mode.copyManagedBitemporal(context, newManagedBitemporal);
                } finally {
                    EventType.RELEASELOCK.create().with(journal).postSynchronous(context);
                }
            } finally {
                journal.unlock();
            }
        } else {
            throw new ConcurrentModificationException(
                    "the journal for id=" + id.toString() + " is locked - try again later");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> retrieve(Query<T> query) {
        Validate.isTrue(query != null, NOTNULL);
        EventType.RETRIEVEDATA.create().with(RetrieveDataEvent.QUERY, query).with(RetrieveDataEvent.BARBEL, this)
                .postBothWay(context);
        return doRetrieveList(() -> (List<T>) backbone.retrieve(query).stream()
                .map(o -> mode.copyManagedBitemporal(context, (Bitemporal) o)).collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> retrieve(Query<T> query, QueryOptions options) {
        Validate.isTrue(query != null && options != null, NOTNULL);
        EventType.RETRIEVEDATA.create().with(RetrieveDataEvent.QUERY, query).with(RetrieveDataEvent.BARBEL, this)
                .postBothWay(context);
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

    public DocumentJournal getDocumentJournal(Object id) {
        Validate.isTrue(id != null, NOTNULL);
        return journals.get(id);
    }

    @Override
    public void load(Collection<Bitemporal> bitemporals) {
        Validate.isTrue(bitemporals != null, "bitemporals cannot be null");
        Validate.isTrue(bitemporals.stream().filter(b -> b.getBitemporalStamp() == null).count() == 0,
                "BitemporalStamp must not be null");
        Validate.isTrue(bitemporals.stream().filter(b -> b.getBitemporalStamp().getDocumentId() == null).count() == 0,
                "document id in BitemporalStamp must not be null");
        List<Object> documentIDs = bitemporals.stream().map(b -> b.getBitemporalStamp().getDocumentId())
                .collect(Collectors.toList());
        for (Object documentId : documentIDs) {
            Validate.validState(backbone.retrieve(BarbelQueries.all(documentId)).isEmpty(),
                    "backbone must not contain any versions of the passed document IDs");
        }
        backbone.addAll(mode.customPersistenceObjectsToManagedBitemporals(context, bitemporals));
    }

    @Override
    public Collection<Bitemporal> unload(Object... documentIDs) {
        Validate.notEmpty(documentIDs, "must pass at least one documentID");
        Validate.validState(!backbone.isEmpty(), "backbone is empty, nothing to unload");
        Collection<Bitemporal> collection = new HashSet<>();
        for (int i = 0; i < documentIDs.length; i++) {
            Object id = documentIDs[i];
            collection.addAll(mode.managedBitemporalToCustomPersistenceObjects(id, backbone));
            backbone.removeAll(backbone.retrieve(BarbelQueries.all(id)).stream().collect(Collectors.toList()));
            journals.remove(id);
        }
        return collection;
    }

    /**
     * Unloads the complete backbone.
     * 
     * @return the complete backbone versions.
     */
    public Collection<Bitemporal> unloadAll() {
        return journals.keySet().stream().flatMap(k -> unload(k).stream()).collect(Collectors.toList());
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

    @SuppressWarnings("unchecked")
    @Override
    public T retrieveOne(Query<T> query) {
        Validate.notNull(query, "query mist not be null");
        EventType.RETRIEVEDATA.create().with(RetrieveDataEvent.QUERY, query).with(RetrieveDataEvent.BARBEL, this)
                .postBothWay(context);
        try {
            return (T) mode.copyManagedBitemporal(context, (Bitemporal) backbone.retrieve(query).uniqueResult());
        } catch (NonUniqueObjectException e) {
            throw new IllegalStateException("your query returned more then one result", e);
        } catch (NoSuchObjectException e) {
            throw new NoSuchElementException("your query returned no result");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T retrieveOne(Query<T> query, QueryOptions options) {
        Validate.notNull(query, "query mist not be null");
        EventType.RETRIEVEDATA.create().with(RetrieveDataEvent.QUERY, query).with(RetrieveDataEvent.BARBEL, this)
                .postBothWay(context);
        try {
            return (T) mode.copyManagedBitemporal(context,
                    (Bitemporal) backbone.retrieve(query, options).uniqueResult());
        } catch (NonUniqueObjectException e) {
            throw new IllegalStateException("your query returned more then one result", e);
        } catch (NoSuchObjectException e) {
            throw new NoSuchElementException("your query returned no result");
        }
    }

}
