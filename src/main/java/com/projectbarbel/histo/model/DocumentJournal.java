package com.projectbarbel.histo.model;

import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collector;

import org.apache.commons.lang3.Validate;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelQueries;
import com.projectbarbel.histo.BarbelQueryOptions;

@SuppressWarnings("rawtypes")
public final class DocumentJournal {

    private final Object id;
    private final IndexedCollection journal;
    private List<Bitemporal> lastInserts;
    private AtomicBoolean locked = new AtomicBoolean();
    private Collector<Object, ?, ConcurrentIndexedCollection<Bitemporal>> objectToBitemporalCollection = Collector
            .of(() -> new ConcurrentIndexedCollection<Bitemporal>(), (c, e) -> c.add((Bitemporal) e), (r1, r2) -> {
                r1.addAll(r2);
                return r1;
            });

    private Collector<Object, ?, List<Bitemporal>> objectToBitemporalList = Collector
            .of(() -> new ArrayList<Bitemporal>(), (c, e) -> c.add((Bitemporal) e), (r1, r2) -> {
                r1.addAll(r2);
                return r1;
            });

    private DocumentJournal(IndexedCollection backbone, Object id) {
        super();
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
    public static DocumentJournal create(IndexedCollection backbone, Object id) {
        Validate.notNull(backbone, "new document list must not be null when creating new journal");
        Validate.notNull(id, "must specify document id for this collection");
        DocumentJournal newjournal = new DocumentJournal(backbone, id);
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
        return "DocumentJournal [journal=" + journal + "]";
    }

    @SuppressWarnings("unchecked")
    public long size() {
        return journal.retrieve(BarbelQueries.all(id)).stream().count();
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> list() {
        return (List<T>) journal
                .retrieve(BarbelQueries.all(id), queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                .stream().collect(objectToBitemporalList);
    }

    @SuppressWarnings("unchecked")
    public <T> IndexedCollection<T> collection() {
        return (IndexedCollection<T>) journal
                .retrieve(BarbelQueries.all(id), queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                .stream().collect(objectToBitemporalCollection);
    }

    public List<Bitemporal> getLastInsert() {
        return lastInserts;
    }

    public Object getId() {
        return id;
    }

    public JournalReader read() {
        return new JournalReader(this, BarbelHistoContext.getDefaultClock());
    }

    public boolean lockAcquired() {
        return locked.compareAndSet(false, true);
    }

    public boolean unlock() {
        return locked.compareAndSet(true, false);
    }
    
    public static class JournalReader {
        private DocumentJournal journal;

        private JournalReader(DocumentJournal journal, Systemclock clock) {
            this.journal = journal;
        }

        public EffectiveReader effectiveTime() {
            return new EffectiveReader(journal);
        }

        @SuppressWarnings("unchecked")
        public <O> List<O> activeVersions() {
            return (List<O>) journal.journal
                    .retrieve(BarbelQueries.allActive(journal.id), BarbelQueryOptions.sortAscendingByEffectiveFrom())
                    .stream().collect(journal.objectToBitemporalList);
        }

        @SuppressWarnings("unchecked")
        public <O> List<O> inactiveVersions() {
            return (List<O>) journal.journal
                    .retrieve(BarbelQueries.allInactive(journal.id), BarbelQueryOptions.sortAscendingByEffectiveFrom())
                    .stream().collect(journal.objectToBitemporalList);
        }
    }

    public static class EffectiveReader {

        private DocumentJournal journal;

        public EffectiveReader(DocumentJournal journal) {
            this.journal = journal;
        }

        @SuppressWarnings("unchecked")
        public <O> List<O> activeVersions() {
            return (List<O>) journal.journal
                    .retrieve(BarbelQueries.allActive(journal.id),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().collect(journal.objectToBitemporalList);
        }

        @SuppressWarnings("unchecked")
        public <O> Optional<O> effectiveNow() {
            return journal.journal
                    .retrieve(BarbelQueries.effectiveNow(journal.id),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().findFirst().flatMap(o -> Optional.of((Bitemporal) o));
        }

        @SuppressWarnings("unchecked")
        public <O> Optional<O> effectiveAt(LocalDate day) {
            return journal.journal
                    .retrieve(BarbelQueries.effectiveAt(journal.id, day),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().findFirst().flatMap(o -> Optional.of((Bitemporal) o));
        }

        @SuppressWarnings("unchecked")
        public <O> IndexedCollection<O> effectiveAfter(LocalDate day) {
            return (IndexedCollection<O>) journal.journal
                    .retrieve(BarbelQueries.effectiveAfter(journal.id, day),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().collect(journal.objectToBitemporalCollection);
        }

        @SuppressWarnings("unchecked")
        public <O> IndexedCollection<O> effectiveBetween(EffectivePeriod period) {
            return (IndexedCollection<O>) journal.journal
                    .retrieve(BarbelQueries.effectiveBetween(journal.id, period),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().collect(journal.objectToBitemporalCollection);
        }

    }

}
