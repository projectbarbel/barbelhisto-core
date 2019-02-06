package com.projectbarbel.histo.journal;

import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelHistoFactory;
import com.projectbarbel.histo.BarbelQueries;
import com.projectbarbel.histo.journal.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.Systemclock;

public class DocumentJournal<T> {

    private IndexedCollection<T> journal;
    private Object id;

    private DocumentJournal(IndexedCollection<T> backbone, Object id) {
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
    public static <T> DocumentJournal<T> create(IndexedCollection<T> backbone, Object id) {
        Validate.notNull(backbone, "new document list must not be null when creating new journal");
        Validate.notNull(id, "must specify document id for this collection");
        DocumentJournal<T> newjournal = (DocumentJournal<T>) new DocumentJournal<T>(backbone, id);
        return newjournal;
    }

    public void update(BiFunction<DocumentJournal<T>, VersionUpdateResult<T>, List<T>> journalUpdateStrategy,
            VersionUpdateResult<T> update) {
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

    public List<T> list() {
        return journal.retrieve(BarbelQueries.all(id), queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                .stream().collect(Collectors.toCollection(ArrayList::new));
    }

    public IndexedCollection<T> collection() {
        return journal.retrieve(BarbelQueries.all(id), queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                .stream().collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
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
                + journal.stream().map(BarbelHistoFactory::prettyPrint).collect(Collectors.joining("\n"));
    }
    // @formatter:on

    public Object getId() {
        return id;
    }

    public JournalReader<T> read() {
        return new JournalReader<T>(this, BarbelHistoContext.getClock());
    }

    public static class JournalReader<T> {
        private DocumentJournal<T> journal;

        private JournalReader(DocumentJournal<T> journal, Systemclock clock) {
            this.journal = journal;
        }

        public EffectiveReader<T> effectiveTime() {
            return new EffectiveReader<T>(journal);
        }

        public RecordtimeReader<T> recordTime() {
            return new RecordtimeReader<T>(journal);
        }

        public List<T> activeVersions() {
            return journal.journal.retrieve(BarbelQueries.allActive(journal.id)).stream().collect(Collectors.toList());
        }

        public List<T> inactiveVersions() {
            return journal.journal.retrieve(BarbelQueries.allInactive(journal.id)).stream()
                    .collect(Collectors.toList());
        }
    }

    public static class EffectiveReader<T> {

        private DocumentJournal<T> journal;

        public EffectiveReader(DocumentJournal<T> journal) {
            this.journal = journal;
        }

        public List<T> activeVersions() {
            return journal.journal
                    .retrieve(BarbelQueries.allActive(journal.id),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().collect(Collectors.toList());
        }

        public Optional<T> effectiveNow() {
            return journal.journal.retrieve(BarbelQueries.effectiveNow(journal.id),
                    queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM)))).stream().findFirst();
        }

        public Optional<T> effectiveAt(LocalDate day) {
            return journal.journal.retrieve(BarbelQueries.effectiveAt(journal.id, day),
                    queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM)))).stream().findFirst();
        }

        public IndexedCollection<T> effectiveAfter(LocalDate day) {
            return journal.journal
                    .retrieve(BarbelQueries.effectiveAfter(journal.id, day),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
        }

        public IndexedCollection<T> effectiveBetween(EffectivePeriod period) {
            return journal.journal
                    .retrieve(BarbelQueries.effectiveBetween(journal.id, period),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
        }

    }

    public static class RecordtimeReader<T> {

        @SuppressWarnings("unused")
        private DocumentJournal<T> journal;

        public RecordtimeReader(DocumentJournal<T> journal) {
            this.journal = journal;
        }

    }

}
