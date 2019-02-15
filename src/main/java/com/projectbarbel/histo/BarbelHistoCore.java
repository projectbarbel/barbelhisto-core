package com.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.descending;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
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

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import com.googlecode.cqengine.resultset.common.NoSuchObjectException;
import com.projectbarbel.histo.functions.DefaultJournalUpdateStrategy.JournalUpdateCase;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.DocumentJournal;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;

public final class BarbelHistoCore<T> implements BarbelHisto<T> {

    public final static ThreadLocal<BarbelHistoContext> CONSTRUCTION_CONTEXT = new ThreadLocal<BarbelHistoContext>();

    private static final String NOTNULL = "all arguments must not be null here";
    
    private final BarbelHistoContext context;
    private final IndexedCollection<T> backbone;
    private final Map<Object, DocumentJournal> journals;
    private final IndexedCollection<UpdateLogRecord> updateLog;
    private final static Map<Object, Object> validTypes = new HashMap<>();

    @SuppressWarnings("unchecked")
    protected BarbelHistoCore(BarbelHistoContext context) {
        CONSTRUCTION_CONTEXT.set(context);
        this.context = Objects.requireNonNull(context);
        this.backbone = Objects.requireNonNull((IndexedCollection<T>) context.getBackboneSupplier().get());
        this.journals = Objects.requireNonNull(context.getJournalStore());
        this.updateLog = Objects.requireNonNull(context.getUpdateLog());
        CONSTRUCTION_CONTEXT.remove();
    }

    @Override
    public boolean save(T newVersion, LocalDate from, LocalDate until) {
        Validate.isTrue(newVersion != null && from != null && until != null, NOTNULL);
        Validate.isTrue(from.isBefore(until), "from date must be before until date");
        T maiden = context.getMode().drawMaiden(context, newVersion);
        validTypes.computeIfAbsent(maiden.getClass(),
                (k) -> context.getMode().validateManagedType(context, (Class<?>) k));
        Object id = context.getMode().drawDocumentId(maiden);
        BitemporalStamp stamp = BitemporalStamp.of(context.getActivity(), id, EffectivePeriod.of(from, until),
                RecordPeriod.createActive(context));
        Bitemporal newManagedBitemporal = context.getMode().snapshotMaiden(context, maiden, stamp);
        BiConsumer<DocumentJournal, Bitemporal> updateStrategy = context.getJournalUpdateStrategyProducer()
                .apply(context);
        DocumentJournal journal = journals.computeIfAbsent(id, (k) -> DocumentJournal.create(backbone, k));
        if (journal.lockAcquired()) {
            updateStrategy.accept(journal, newManagedBitemporal);
        } else {
            throw new ConcurrentModificationException("the journal for id=" + id.toString() + " is locked - try again later");
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
                .map(o -> context.getMode().copyManagedBitemporal(context, (Bitemporal) o))
                .collect(Collectors.toList()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> retrieve(Query<T> query, QueryOptions options) {
        Validate.isTrue(query != null && options != null, NOTNULL);
        return doRetrieveList(() -> (List<T>) backbone.retrieve(query, options).stream()
                .map(o -> context.getMode().copyManagedBitemporal(context, (Bitemporal) o))
                .collect(Collectors.toList()));
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
                .retrieve(equal(UpdateLogRecord.USER, context.getUser()),
                        queryOptions(orderBy(descending(UpdateLogRecord.TIMESTAMP))))
                .stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("not update performed by this user yet"));
    }

    public DocumentJournal getDocumentJournal(Object id) {
        Validate.isTrue(id != null, NOTNULL);
        return journals.get(id);
    }

    public static class UpdateLogRecord {

        public static final Attribute<UpdateLogRecord, String> USER = new SimpleAttribute<UpdateLogRecord, String>(
                "user") {
            public String getValue(UpdateLogRecord logEntry, QueryOptions queryOptions) {
                return logEntry.user;
            }
        };

        public static final Attribute<UpdateLogRecord, ChronoZonedDateTime<LocalDate>> TIMESTAMP = new SimpleAttribute<UpdateLogRecord, ChronoZonedDateTime<LocalDate>>(
                "timestamp") {
            public ZonedDateTime getValue(UpdateLogRecord logEntry, QueryOptions queryOptions) {
                return logEntry.timestamp;
            }
        };

        public final ZonedDateTime timestamp;
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
            this.timestamp = ZonedDateTime.now();
            this.user = user;
        }
    }

    @Override
    public void populate(Collection<Bitemporal> bitemporals) {
        Validate.isTrue(bitemporals != null, "bitemporals cannot be null");
        Validate.validState(backbone.size() == 0, "backbone must be empty when calling populate");
        backbone.addAll(context.getMode().customPersistenceObjectsToManagedBitemporals(context, bitemporals));
    }

    @Override
    public Collection<Bitemporal> dump(DumpMode mode) {
        Validate.isTrue(mode != null, NOTNULL);
        Collection<Bitemporal> collection = context.getMode().managedBitemporalToCustomPersistenceObjects(backbone);
        if (mode.equals(DumpMode.CLEARCOLLECTION))
            backbone.clear();
        return collection;
    }

    @Override
    public DocumentJournal timeshift(Object id, LocalDateTime time) {
        Validate.isTrue(id != null && time!=null, NOTNULL);
        ResultSet<T> result = backbone.retrieve(BarbelQueries.journalAt(id, time));
        return DocumentJournal
                .create(result.stream().map(d -> context.getMode().copyManagedBitemporal(context, (Bitemporal) d))
                        .map(d -> d.getBitemporalStamp().getRecordTime().activate())
                        .collect(Collectors.toCollection(ConcurrentIndexedCollection::new)), id);
    }

    private List<T> doRetrieveList(Supplier<List<T>> retrieveOperation) {
        try {
            return retrieveOperation.get();
        } catch (Exception e) {
            if ((e instanceof ClassCastException) && e.getMessage().contains("Bitemporal"))
                throw new RuntimeException(
                        "a ClassCastException was thrown on retrieval of items - maybe using persistence and forgot to add @PersistenceConfig(serializer=BarbelPojoSerializer.class) to the pojo?",
                        e);
            throw e;
        }
    }
    
    public enum DumpMode {
        CLEARCOLLECTION, READONLY;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<T> retrieveOne(Query<T> query) {
        try {
            T object = (T)context.getMode().copyManagedBitemporal(context, (Bitemporal)backbone.retrieve(query).uniqueResult());
            return Optional.of(object);
        } catch (NoSuchObjectException e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<T> retrieveOne(Query<T> query, QueryOptions options) {
        try {
            T object = (T)context.getMode().copyManagedBitemporal(context, (Bitemporal)backbone.retrieve(query, options).uniqueResult());
            return Optional.of(object);
        } catch (NoSuchObjectException e) {
            return Optional.empty();
        }
    }

}
