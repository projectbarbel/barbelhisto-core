package org.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.EffectivePeriod;
import org.projectbarbel.histo.model.Systemclock;

import com.googlecode.cqengine.IndexedCollection;

/**
 * Document Journal abstraction to work on for internal processing in
 * {@link BarbelHistoCore} and for the client. When client work on instances of
 * {@link DocumentJournal} it is essential that {@link ProcessingState} is set
 * to {@link ProcessingState#EXTERNAL}. This ensures that clients only get copies
 * of journal objects.
 * 
 * @author niklasschlimm
 *
 */
@SuppressWarnings("rawtypes")
public final class DocumentJournal {

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
    private List<Bitemporal> lastInserts;
    private AtomicBoolean locked = new AtomicBoolean();
    private final BarbelHistoContext context;
    private final ProcessingState processingState;

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
     * @param backbone the collection containing the journal objects
     * @param id       document id of the document under barbel control
     * @return the {@link DocumentJournal} created
     */
    public static DocumentJournal create(ProcessingState processingState, BarbelHistoContext context,
            IndexedCollection backbone, Object id) {
        Validate.notNull(backbone, "new document list must not be null when creating new journal");
        Validate.notNull(id, "must specify document id for this collection");
        Validate.notNull(context, "must specify context for this journal");
        DocumentJournal newjournal = new DocumentJournal(processingState, context, backbone, id);
        return newjournal;
    }

    @SuppressWarnings("unchecked")
    public void accept(List<Bitemporal> newVersions) {
        Validate.isTrue(
                newVersions.stream().filter(d -> !d.getBitemporalStamp().getDocumentId().equals(id)).count() == 0,
                "new versions must match document id of journal");
        newVersions.sort((v1, v2) -> v1.getBitemporalStamp().getEffectiveTime().from()
                .isBefore(v2.getBitemporalStamp().getEffectiveTime().from()) ? -1 : 1);
        newVersions.sort((v1, v2) -> v1.getBitemporalStamp().getEffectiveTime().until()
                .isBefore(v2.getBitemporalStamp().getEffectiveTime().until()) ? -1 : 1);
        this.lastInserts = newVersions;
        journal.addAll(newVersions);
    }

    @Override
    public String toString() {
        return "DocumentJournal [id=" + id + ", lastInserts=" + lastInserts + ", locked=" + locked + "]";
    }

    @SuppressWarnings("unchecked")
    public long size() {
        return journal.retrieve(BarbelQueries.all(id)).stream().count();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> list() {
        return (List<T>) journal
                .retrieve(BarbelQueries.all(id), queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                .stream().map(d -> processingState.expose(context, (Bitemporal) d)).collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    public <T> IndexedCollection<T> collection() {
        return (IndexedCollection<T>) journal
                .retrieve(BarbelQueries.all(id), queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                .stream().map(d -> processingState.expose(context, (Bitemporal) d)).collect(Collectors.toList());
    }

    protected List<Bitemporal> getLastInsert() {
        return lastInserts;
    }

    public Object getId() {
        return id;
    }

    public JournalReader read() {
        return new JournalReader(this, BarbelHistoContext.getDefaultClock());
    }

    protected boolean lockAcquired() {
        return locked.compareAndSet(false, true);
    }

    protected boolean unlock() {
        return locked.compareAndSet(true, false);
    }

    public static class JournalReader {
        private DocumentJournal journal;

        private JournalReader(DocumentJournal journal, Systemclock clock) {
            this.journal = journal;
        }

        @SuppressWarnings("unchecked")
        public <O> List<O> activeVersions() {
            return (List<O>) journal.journal
                    .retrieve(BarbelQueries.allActive(journal.id), BarbelQueryOptions.sortAscendingByEffectiveFrom())
                    .stream().map(d -> journal.processingState.expose(journal.context, (Bitemporal) d))
                    .collect(Collectors.toList());
        }

        @SuppressWarnings("unchecked")
        public <O> List<O> inactiveVersions() {
            return (List<O>) journal.journal
                    .retrieve(BarbelQueries.allInactive(journal.id), BarbelQueryOptions.sortAscendingByEffectiveFrom())
                    .stream().collect(Collectors.toList());
        }

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

        @SuppressWarnings("unchecked")
        public <O> List<O> effectiveAfter(LocalDate day) {
            return (List<O>) journal.journal
                    .retrieve(BarbelQueries.effectiveAfter(journal.id, day),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().map(d -> journal.processingState.expose(journal.context, (Bitemporal) d))
                    .collect(Collectors.toList());
        }

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
