package com.projectbarbel.histo.api;

import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.equal;
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
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.resultset.ResultSet;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.api.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.functions.journal.BitemporalCollectionPreparedStatements;
import com.projectbarbel.histo.functions.journal.JournalUpdateStrategyEmbedding;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.Systemclock;

public class DocumentJournal<T extends Bitemporal<?>> {

    private IndexedCollection<T> journal;
    private BiFunction<IndexedCollection<T>, LocalDate, ResultSet<T>> effectiveReaderFunction = BitemporalCollectionPreparedStatements::getActiveVersionEffectiveOn_ByDate;
    private BiFunction<IndexedCollection<T>, LocalDate, ResultSet<T>> effectiveAfterFunction = BitemporalCollectionPreparedStatements::getActiveVersionsEffectiveAfter_ByDate_orderByEffectiveFrom;
    private BiFunction<IndexedCollection<T>, EffectivePeriod, ResultSet<T>> effectiveBetweenFunction = BitemporalCollectionPreparedStatements::getActiveVersionsEffectiveBetween_ByFromAndUntilDate_orderByEffectiveFrom;
    private BiFunction<DocumentJournal<T>, VersionUpdateResult<T>, List<T>> updateFunction = new JournalUpdateStrategyEmbedding<T>();
    private Object id;

    private DocumentJournal(IndexedCollection<T> backbone, Object id) {
        super();
        this.journal = backbone;
        this.id = id;
    }

    public static <T extends Bitemporal<?>> DocumentJournal<T> create(IndexedCollection<T> backbone, Object id) {
        Validate.notNull(backbone, "new document list must not be null when creating new journal");
        Validate.notNull(id, "must specify document id for this collection");
        DocumentJournal<T> newjournal = (DocumentJournal<T>) new DocumentJournal<T>(backbone, id);
        return newjournal;
    }

    public void update(VersionUpdateResult<T> update) {
        Validate.notNull(update, "update passed must not be null");
        Validate.validState(journal.contains(update.oldVersion()),
                "the old version of that update passed is unknown in this journal - please only add valid update whose origin are objects from this journal");
        Validate.validState(!journal.contains(update.newPrecedingVersion()),
                "the new generated preceeding version by this update was already added to the journal");
        Validate.validState(!journal.contains(update.newSubsequentVersion()),
                "the new generated subsequent version by this update was already added to the journal");
        if (journal.contains(update.oldVersion())) {
            journal.addAll(updateFunction.apply(this, update));
        }
    }

    @Override
    public String toString() {
        return "DocumentJournal [journal=" + journal + "]";
    }

    public long size() {
        return journal.stream().filter(d -> d.getDocumentId().equals(id)).count();
    }

    @SuppressWarnings("unchecked")
    public List<T> list() {
        return journal
                .retrieve(equal((Attribute<T, String>) Bitemporal.DOCUMENT_ID, (String) id),
                        queryOptions(orderBy(ascending(Bitemporal.EFFECTIVE_FROM))))
                .stream().collect(Collectors.toCollection(ArrayList::new));
    }

    @SuppressWarnings("unchecked")
    public IndexedCollection<T> collection() {
        return journal
                .retrieve(equal((Attribute<T, String>) Bitemporal.DOCUMENT_ID, (String) id),
                        queryOptions(orderBy(ascending(Bitemporal.EFFECTIVE_FROM))))
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
                + journal.stream().map(Bitemporal::prettyPrint).collect(Collectors.joining("\n"));
    }
    // @formatter:on

    public Object getId() {
        return id;
    }

    public JournalReader<T> read() {
        return new JournalReader<T>(this, BarbelHistoContext.getClock());
    }

    public static class JournalReader<T extends Bitemporal<?>> {
        private DocumentJournal<T> journal;
        private Systemclock clock;

        private JournalReader(DocumentJournal<T> journal, Systemclock clock) {
            this.journal = journal;
            this.clock = clock;
        }

        public EffectiveReader<T> effectiveTime() {
            return new EffectiveReader<T>(journal, clock);
        }

        public RecordtimeReader<T> recordTime() {
            return new RecordtimeReader<T>(journal);
        }

        public List<T> activeVersions() {
            return journal.list().stream().filter(d -> d.getDocumentId().equals(journal.id)).filter((d) -> d.isActive())
                    .collect(Collectors.toList());
        }

        public List<T> inactiveVersions() {
            return journal.list().stream().filter(d -> d.getDocumentId().equals(journal.id))
                    .filter((d) -> !d.isActive()).collect(Collectors.toList());
        }
    }

    public static class EffectiveReader<T extends Bitemporal<?>> {

        private DocumentJournal<T> journal;
        private Systemclock clock;

        public EffectiveReader(DocumentJournal<T> journal, Systemclock clock) {
            this.journal = journal;
            this.clock = clock;
        }

        public List<T> activeVersions() {
            return journal.list().stream().filter(d -> d.getDocumentId().equals(journal.id)).filter((d) -> d.isActive())
                    .collect(Collectors.toList());
        }

        public Optional<T> effectiveNow() {
            ResultSet<T> result = journal.effectiveReaderFunction.apply(journal.collection(),
                    clock.now().toLocalDate());
            return result.iterator().hasNext() ? Optional.of(result.iterator().next()) : Optional.empty();
        }

        public Optional<T> effectiveAt(LocalDate day) {
            ResultSet<T> result = journal.effectiveReaderFunction.apply(journal.collection(), day);
            return result.iterator().hasNext() ? Optional.of(result.iterator().next()) : Optional.empty();
        }

        public IndexedCollection<T> effectiveAfter(LocalDate day) {
            return journal.effectiveAfterFunction.apply(journal.collection(), day).stream()
                    .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
        }

        public IndexedCollection<T> effectiveBetween(EffectivePeriod effectiveTime) {
            return journal.effectiveBetweenFunction.apply(journal.collection(), effectiveTime).stream()
                    .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
        }

    }

    public static class RecordtimeReader<T extends Bitemporal<?>> {

        @SuppressWarnings("unused")
        private DocumentJournal<T> journal;

        public RecordtimeReader(DocumentJournal<T> journal) {
            this.journal = journal;
        }

    }

}
