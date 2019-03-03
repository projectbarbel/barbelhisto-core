package org.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.projectbarbel.histo.event.EventType;
import org.projectbarbel.histo.event.EventType.InsertBitemporalEvent;
import org.projectbarbel.histo.event.EventType.InactivationEvent;
import org.projectbarbel.histo.functions.EmbeddingJournalUpdateStrategy.JournalUpdateCase;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.DefaultPojo;
import org.projectbarbel.histo.model.EffectivePeriod;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;

/**
 * Document Journal abstraction to work on for internal processing in
 * {@link BarbelHistoCore} and for the client. When clients work on instances of
 * {@link DocumentJournal} it is essential that {@link ProcessingState} is set
 * to {@link ProcessingState#EXTERNAL}. This ensures that clients only get
 * copies of journal objects.
 * 
 * @author Niklas Schlimm
 *
 */
@SuppressWarnings("rawtypes")
public final class DocumentJournal {

    private static final String FORBIDDEN_OPERATION = "you're not allowed to use this operation";
    public static final DocumentJournal EMPTYSAMPLE = DocumentJournal.create(ProcessingState.EXTERNAL,
            BarbelHistoBuilder.barbel(), new ConcurrentIndexedCollection<DefaultPojo>(), "unknown");

    public enum ProcessingState {
        // @formatter:off
        // internal processing -> return original objects
        INTERNAL((c, o) -> o),
        // return copies to user clients
        EXTERNAL((c, o) -> c.getMode().copyManagedBitemporal(c, (Bitemporal) o));
        // @formatter:on
        private BiFunction<BarbelHistoContext, Bitemporal, Bitemporal> exposer;

        private ProcessingState(BiFunction<BarbelHistoContext, Bitemporal, Bitemporal> exposer) {
            this.exposer = exposer;
        }

        private Bitemporal expose(BarbelHistoContext context, Bitemporal object) {
            return exposer.apply(context, object);
        }

    }

    private final Object id;
    private final IndexedCollection journal;
    private final List<Bitemporal> lastInserts = new ArrayList<>();
    private final AtomicBoolean locked = new AtomicBoolean();
    private final BarbelHistoContext context;
    private final ProcessingState processingState;
    private final Set<Inactivation> lastInactivations = new HashSet<>();
    private JournalUpdateCase lastUpdateCase;
    private Bitemporal lastUpdateRequest;

    private DocumentJournal(ProcessingState processingState, BarbelHistoContext context, IndexedCollection backbone,
            Object id) {
        super();
        this.processingState = processingState;
        this.context = context;
        this.journal = backbone;
        this.id = id;
    }

    /**
     * Creates the journal using the backbone as pre-created collection. This
     * collection may contain objects with other Ids.
     * 
     * @param processingState the {@link ProcessingState}
     * @param context         the current context
     * @param backbone        the collection containing the journal objects
     * @param id              document id of the document under barbel control
     * @return the {@link DocumentJournal} created
     */
    public static DocumentJournal create(ProcessingState processingState, BarbelHistoContext context,
            IndexedCollection backbone, Object id) {
        Validate.notNull(backbone, "new document list must not be null when creating new journal");
        Validate.notNull(id, "must specify document id for this collection");
        Validate.notNull(context, "must specify context for this journal");
        return new DocumentJournal(processingState, context, backbone, id);
    }

    /**
     * Create {@link DocumentJournal} from context.
     * 
     * @param processingState the processing state
     * @param context         the context
     * @param id              the document journal id
     * @return new {@link DocumentJournal}
     */
    public static DocumentJournal create(ProcessingState processingState, BarbelHistoContext context, Object id) {
        return create(processingState, context, context.getBackbone(), id);
    }

    @SuppressWarnings("unchecked")
    public void insert(List<Bitemporal> newVersions) {
        Validate.validState(ProcessingState.INTERNAL.equals(processingState), FORBIDDEN_OPERATION);
        Validate.isTrue(
                newVersions.stream().filter(d -> !d.getBitemporalStamp().getDocumentId().equals(id)).count() == 0,
                "new versions must match document id of journal");
        newVersions.sort((v1, v2) -> v1.getBitemporalStamp().getEffectiveTime().from()
                .isBefore(v2.getBitemporalStamp().getEffectiveTime().from()) ? -1 : 1);
        newVersions.sort((v1, v2) -> v1.getBitemporalStamp().getEffectiveTime().until()
                .isBefore(v2.getBitemporalStamp().getEffectiveTime().until()) ? -1 : 1);
        this.lastInserts.addAll(newVersions);
        EventType.INSERTBITEMPORAL.create().with(this).with(InsertBitemporalEvent.NEWVERSIONS, copyList(newVersions))
                .postBothWay(context);
        journal.addAll(newVersions);
    }

    public void inactivate(Bitemporal objectToInactivate, Bitemporal inactivatedCopy) {
        Validate.validState(ProcessingState.INTERNAL.equals(processingState), FORBIDDEN_OPERATION);
        Validate.isTrue(objectToInactivate.getBitemporalStamp().getDocumentId().equals(id),
                "objects must match document id of journal");
        Validate.isTrue(inactivatedCopy.getBitemporalStamp().getDocumentId().equals(id),
                "objects must match document id of journal");
        EventType.INACTIVATION.create().with(journal)
                .with(InactivationEvent.OBJECT_REMOVED,
                        context.getMode().copyManagedBitemporal(context, objectToInactivate))
                .with(InactivationEvent.OBJECT_ADDED, context.getMode().copyManagedBitemporal(context, inactivatedCopy))
                .postBothWay(context);
        if (lastInactivations.add(new Inactivation(objectToInactivate, inactivatedCopy)))
            replace(objectToInactivate, inactivatedCopy);
    }

    private List<Bitemporal> copyList(List<Bitemporal> objectsToRemove) {
        return objectsToRemove.stream().map(v -> context.getMode().copyManagedBitemporal(context, v))
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private void replace(Bitemporal objectsToRemove, Bitemporal objectsToAdd) {
        journal.update(Collections.singletonList(objectsToRemove), Collections.singletonList(objectsToAdd));
    }

    @Override
    public String toString() {
        return "DocumentJournal [id=" + id + ", lastInserts=" + lastInserts + ", locked=" + locked + "]";
    }

    @SuppressWarnings("unchecked")
    public long size() {
        return journal.retrieve(BarbelQueries.all(id)).stream().count();
    }

    /**
     * Get the complete archive for the current document id as list.
     * 
     * @return the archive
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> list() {
        return (List<T>) journal
                .retrieve(BarbelQueries.all(id), queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                .stream().map(d -> processingState.expose(context, (Bitemporal) d)).collect(Collectors.toList());
    }

    /**
     * Get the complete archive for the current document id as collection.
     * 
     * @return the archive
     */
    @SuppressWarnings("unchecked")
    public <T> IndexedCollection<T> collection() {
        return (IndexedCollection<T>) journal
                .retrieve(BarbelQueries.all(id), queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                .stream().map(d -> processingState.expose(context, (Bitemporal) d)).collect(Collectors.toList());
    }

    public List<Bitemporal> getLastInsert() {
        return lastInserts;
    }

    public Set<Inactivation> getLastInactivations() {
        return lastInactivations;
    }

    public Object getId() {
        return id;
    }

    public ProcessingState getProcessingState() {
        return processingState;
    }

    /**
     * Read from the journal.
     * 
     * @return the reader
     */
    public JournalReader read() {
        return new JournalReader(this);
    }

    /**
     * Acquire a lock on this journal.
     * 
     * @return true if lock acquired
     */
    protected boolean lockAcquired() {
        if (locked.compareAndSet(false, true)) {
            lastInserts.clear();
            lastInactivations.clear();
            lastUpdateCase = null;
            lastUpdateRequest = null;
            return true;
        } else
            return false;
    }

    protected boolean unlock() {
        return locked.compareAndSet(true, false);
    }

    public JournalUpdateCase getLastUpdateCase() {
        return lastUpdateCase;
    }

    public void setLastUpdateCase(JournalUpdateCase lastUpdateCase) {
        Validate.validState(ProcessingState.INTERNAL.equals(processingState), FORBIDDEN_OPERATION);
        this.lastUpdateCase = lastUpdateCase;
    }

    public Bitemporal getLastUpdateRequest() {
        return lastUpdateRequest;
    }

    protected void setLastUpdateRequest(Bitemporal lastUpdateRequest) {
        this.lastUpdateRequest = lastUpdateRequest;
    }

    public static class Inactivation {
        private final Bitemporal objectRemoved;
        private final Bitemporal objectAdded;

        private Inactivation(Bitemporal objectRemoved, Bitemporal objectAdded) {
            this.objectRemoved = objectRemoved;
            this.objectAdded = objectAdded;
        }

        public Bitemporal getObjectRemoved() {
            return objectRemoved;
        }

        public Bitemporal getObjectAdded() {
            return objectAdded;
        }

        @Override
        public int hashCode() {
            return Objects.hash(objectAdded.getBitemporalStamp().getVersionId(),
                    objectRemoved.getBitemporalStamp().getVersionId());
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (!(obj instanceof Inactivation))
                return false;
            Inactivation other = (Inactivation) obj;
            return Objects.equals(objectAdded.getBitemporalStamp().getVersionId(),
                    other.objectAdded.getBitemporalStamp().getVersionId())
                    && Objects.equals(objectRemoved.getBitemporalStamp().getVersionId(),
                            other.objectRemoved.getBitemporalStamp().getVersionId());
        }

    }

    public static class JournalReader {
        private DocumentJournal journal;

        private JournalReader(DocumentJournal journal) {
            this.journal = journal;
        }

        /**
         * Get the versions currently active.
         * 
         * @return the active versions
         */
        @SuppressWarnings("unchecked")
        public <O> List<O> activeVersions() {
            return (List<O>) journal.journal
                    .retrieve(BarbelQueries.allActive(journal.id), BarbelQueryOptions.sortAscendingByEffectiveFrom())
                    .stream().map(d -> journal.processingState.expose(journal.context, (Bitemporal) d))
                    .collect(Collectors.toList());
        }

        /**
         * Get the inactivated versions. Versions get inactivated when new versions are
         * posted and there versions cross their effective periods.
         * 
         * @return the inactive versions
         */
        @SuppressWarnings("unchecked")
        public <O> List<O> inactiveVersions() {
            return (List<O>) journal.journal
                    .retrieve(BarbelQueries.allInactive(journal.id), BarbelQueryOptions.sortAscendingByEffectiveFrom())
                    .stream().collect(Collectors.toList());
        }

        /**
         * The active version effective today. The "current" state of the object.
         * 
         * @return the effective version
         */
        @SuppressWarnings("unchecked")
        public <O> Optional<O> effectiveNow() {
            return journal.journal
                    .retrieve(BarbelQueries.effectiveNow(journal.id),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().map(d -> journal.processingState.expose(journal.context, (Bitemporal) d)).findFirst()
                    .flatMap(o -> Optional.of((Bitemporal) o));
        }

        @SuppressWarnings("unchecked")
        public <O> Optional<O> effectiveAt(LocalDate day) {
            return journal.journal
                    .retrieve(BarbelQueries.effectiveAt(journal.id, day),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().map(d -> journal.processingState.expose(journal.context, (Bitemporal) d)).findFirst()
                    .flatMap(o -> Optional.of((Bitemporal) o));
        }

        /**
         * The active versions after the given date. If due date is set to today, the
         * query returns all the future versions that will become effective.
         * 
         * @param day the due date
         * @return the active versions
         */
        @SuppressWarnings("unchecked")
        public <O> List<O> effectiveAfter(LocalDate day) {
            return (List<O>) journal.journal
                    .retrieve(BarbelQueries.effectiveAfter(journal.id, day),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().map(d -> journal.processingState.expose(journal.context, (Bitemporal) d))
                    .collect(Collectors.toList());
        }

        /**
         * Get the active versions effective within the given {@link EffectivePeriod}.
         * 
         * @param period the intervall
         * @return the active versions
         */
        @SuppressWarnings("unchecked")
        public <O> List<O> effectiveBetween(EffectivePeriod period) {
            return (List<O>) journal.journal
                    .retrieve(BarbelQueries.effectiveBetween(journal.id, period),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().map(d -> journal.processingState.expose(journal.context, (Bitemporal) d))
                    .collect(Collectors.toList());
        }

    }

}
