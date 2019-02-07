package com.projectbarbel.histo.journal;

import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelQueries;
import com.projectbarbel.histo.journal.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.Systemclock;

public class DocumentJournal {

    private IndexedCollection<Object> journal;
    private Object id;
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
    
    private DocumentJournal(IndexedCollection<Object> backbone, Object id) {
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
     * @return
     */
    public static DocumentJournal create(IndexedCollection<Object> backbone, Object id) {
        Validate.notNull(backbone, "new document list must not be null when creating new journal");
        Validate.notNull(id, "must specify document id for this collection");
        DocumentJournal newjournal = new DocumentJournal(backbone, id);
        return newjournal;
    }

    public void update(BiFunction<DocumentJournal, VersionUpdateResult, List<Object>> journalUpdateStrategy,
            VersionUpdateResult update) {
        Validate.notNull(update, "update passed must not be null");
        Validate.validState(journal.contains(update.oldVersion()),
                "the old version of that update passed is unknown in this journal - please only add valid update whose origin are objects from this journal");
        Validate.validState(!journal.contains(update.newPrecedingVersion()),
                "the new generated preceeding version by this update was already added to the journal");
        Validate.validState(!journal.contains(update.newSubsequentVersion()),
                "the new generated subsequent version by this update was already added to the journal");
        if (journal.contains(update.oldVersion())) {
            journal.addAll(journalUpdateStrategy.apply(this, update));
        }
    }

    @Override
    public String toString() {
        return "DocumentJournal [journal=" + journal + "]";
    }

    public long size() {
        return journal.retrieve(BarbelQueries.all(id)).stream().count();
    }

    public List<Bitemporal> list() {
        return journal
                .retrieve(BarbelQueries.all(id), queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                .stream().collect(objectToBitemporalList);
    }

    public IndexedCollection<Bitemporal> collection() {
        return journal.retrieve(BarbelQueries.all(id), queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                .stream().collect(objectToBitemporalCollection);
    }

    // @formatter:off
    public String prettyPrint() {
        return "\n" + "Document-ID: " + (journal.size() > 0 ? id : "<empty jounral>")
                + "\n\n"
                + String.format("|%-40s|%-15s|%-16s|%-8s|%-21s|%-23s|%-21s|%-23s|", "Version-ID", "Effective-From",
                        "Effective-Until", "State", "Created-By", "Created-At", "Inactivated-By", "Inactivated-At")
                + "\n|" + StringUtils.leftPad("|", 41, "-") + StringUtils.leftPad("|", 16, "-")
                + StringUtils.leftPad("|", 17, "-") + StringUtils.leftPad("|", 9, "-")
                + StringUtils.leftPad("|", 22, "-") + StringUtils.leftPad("|", 24, "-")
                + StringUtils.leftPad("|", 22, "-") + StringUtils.leftPad("|", 24, "-") + "\n"
                + journal.stream().map(d->BarbelHistoFactory.prettyPrint((Bitemporal)d)).collect(Collectors.joining("\n"));
    }
    // @formatter:on

    public Object getId() {
        return id;
    }

    public JournalReader read() {
        return new JournalReader(this, BarbelHistoContext.getClock());
    }

    public static class JournalReader {
        private DocumentJournal journal;

        private JournalReader(DocumentJournal journal, Systemclock clock) {
            this.journal = journal;
        }

        public EffectiveReader effectiveTime() {
            return new EffectiveReader(journal);
        }

        public RecordtimeReader recordTime() {
            return new RecordtimeReader(journal);
        }

        public List<Bitemporal> activeVersions() {
            return journal.journal.retrieve(BarbelQueries.allActive(journal.id)).stream().collect(journal.objectToBitemporalList);
        }

        public List<Bitemporal> inactiveVersions() {
            return journal.journal.retrieve(BarbelQueries.allInactive(journal.id)).stream()
                    .collect(journal.objectToBitemporalList);
        }
    }

    public static class EffectiveReader {

        private DocumentJournal journal;

        public EffectiveReader(DocumentJournal journal) {
            this.journal = journal;
        }

        public List<Bitemporal> activeVersions() {
            return journal.journal
                    .retrieve(BarbelQueries.allActive(journal.id),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().collect(journal.objectToBitemporalList);
        }

        public Optional<Bitemporal> effectiveNow() {
            return journal.journal.retrieve(BarbelQueries.effectiveNow(journal.id),
                    queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM)))).stream().findFirst().flatMap(o->Optional.of((Bitemporal)o));
        }

        public Optional<Bitemporal> effectiveAt(LocalDate day) {
            return journal.journal.retrieve(BarbelQueries.effectiveAt(journal.id, day),
                    queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM)))).stream().findFirst().flatMap(o->Optional.of((Bitemporal)o));
        }

        public IndexedCollection<Bitemporal> effectiveAfter(LocalDate day) {
            return journal.journal
                    .retrieve(BarbelQueries.effectiveAfter(journal.id, day),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().collect(journal.objectToBitemporalCollection);
        }

        public IndexedCollection<Bitemporal> effectiveBetween(EffectivePeriod period) {
            return journal.journal
                    .retrieve(BarbelQueries.effectiveBetween(journal.id, period),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().collect(journal.objectToBitemporalCollection);
        }

    }

    public static class RecordtimeReader {

        @SuppressWarnings("unused")
        private DocumentJournal journal;

        public RecordtimeReader(DocumentJournal journal) {
            this.journal = journal;
        }

    }

}
