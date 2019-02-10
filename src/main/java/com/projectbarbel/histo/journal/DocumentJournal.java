package com.projectbarbel.histo.journal;

import static com.googlecode.cqengine.query.QueryFactory.ascending;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.BarbelQueries;
import com.projectbarbel.histo.BarbelQueryOptions;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.Systemclock;

public class DocumentJournal implements Consumer<List<Bitemporal>> {

    private Object id;
    private IndexedCollection<Object> journal;
    private List<Bitemporal> lastInserts;
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

    public void accept(List<Bitemporal> newVersions) {
        Validate.isTrue(
                newVersions.stream().filter(d -> !d.getBitemporalStamp().getDocumentId().equals(id)).count() == 0,
                "new versions must match document id of journal");
        newVersions.sort((v1,v2)->v1.getBitemporalStamp().getEffectiveTime().from().isBefore(v2.getBitemporalStamp().getEffectiveTime().from())?-1:1);
        newVersions.sort((v1,v2)->v1.getBitemporalStamp().getEffectiveTime().until().isBefore(v2.getBitemporalStamp().getEffectiveTime().until())?-1:1);
        this.lastInserts = newVersions;
        journal.addAll(newVersions);
    }

    @Override
    public String toString() {
        return "DocumentJournal [journal=" + journal + "]";
    }

    public long size() {
        return journal.retrieve(BarbelQueries.all(id)).stream().count();
    }

    public List<Bitemporal> list() {
        return journal.retrieve(BarbelQueries.all(id), queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                .stream().collect(objectToBitemporalList);
    }

    public IndexedCollection<Bitemporal> collection() {
        return journal.retrieve(BarbelQueries.all(id), queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                .stream().collect(objectToBitemporalCollection);
    }

    public List<Bitemporal> getLastInsert() {
        return lastInserts;
    }

    // @formatter:off
    public static String prettyPrint(IndexedCollection<Bitemporal> journal, Object id, Function<Bitemporal, String> customField) {
        return "\n" + "Document-ID: " + (journal.size() > 0 ? id : "<empty jounral>")
                + "\n\n"
                + String.format("|%-40s|%-15s|%-16s|%-8s|%-21s|%-45s|%-21s|%-45s|%-31s|", "Version-ID", "Effective-From",
                        "Effective-Until", "State", "Created-By", "Created-At", "Inactivated-By", "Inactivated-At", "Data")
                + "\n|" + StringUtils.leftPad("|", 41, "-") + StringUtils.leftPad("|", 16, "-")
                + StringUtils.leftPad("|", 17, "-") + StringUtils.leftPad("|", 9, "-")
                + StringUtils.leftPad("|", 22, "-") + StringUtils.leftPad("|", 46, "-")
                + StringUtils.leftPad("|", 22, "-") + StringUtils.leftPad("|", 46, "-") + StringUtils.leftPad("|", 32, "-") + 
                "\n"
                + journal.retrieve(BarbelQueries.all(id), BarbelQueryOptions.sortAscendingByEffectiveFrom()).stream()
                     .map(d -> prettyPrint(d, customField)).collect(Collectors.joining("\n"));
    }

    private static String prettyPrint(Bitemporal bitemporal, Function<Bitemporal, String> customField) {
        return String.format("|%1$-40s|%2$-15tF|%3$-16tF|%4$-8s|%5$-21s|%6$-45s|%7$-21s|%8$-45s|%9$-31s|",
                bitemporal.getBitemporalStamp().getVersionId(), bitemporal.getBitemporalStamp().getEffectiveTime().from(),
                bitemporal.getBitemporalStamp().getEffectiveTime().until(),
                bitemporal.getBitemporalStamp().getRecordTime().getState().name(),
                StringUtils.truncate(bitemporal.getBitemporalStamp().getRecordTime().getCreatedBy(), 20),
                DateTimeFormatter.ISO_ZONED_DATE_TIME
                        .format(bitemporal.getBitemporalStamp().getRecordTime().getCreatedAt()),
                        StringUtils.truncate(bitemporal.getBitemporalStamp().getRecordTime().getInactivatedBy(), 20),
                        DateTimeFormatter.ISO_ZONED_DATE_TIME
                        .format(bitemporal.getBitemporalStamp().getRecordTime().getInactivatedAt()), StringUtils.truncate(customField.apply(bitemporal),30));
    }
    // @formatter:on

    public Object getId() {
        return id;
    }

    public JournalReader read() {
        return new JournalReader(this, BarbelHistoContext.getDefaultClock());
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
            return journal.journal.retrieve(BarbelQueries.allActive(journal.id), BarbelQueryOptions.sortAscendingByEffectiveFrom()).stream()
                    .collect(journal.objectToBitemporalList);
        }

        public List<Bitemporal> inactiveVersions() {
            return journal.journal.retrieve(BarbelQueries.allInactive(journal.id), BarbelQueryOptions.sortAscendingByEffectiveFrom()).stream()
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
            return journal.journal
                    .retrieve(BarbelQueries.effectiveNow(journal.id),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().findFirst().flatMap(o -> Optional.of((Bitemporal) o));
        }

        public Optional<Bitemporal> effectiveAt(LocalDate day) {
            return journal.journal
                    .retrieve(BarbelQueries.effectiveAt(journal.id, day),
                            queryOptions(orderBy(ascending(BarbelQueries.EFFECTIVE_FROM))))
                    .stream().findFirst().flatMap(o -> Optional.of((Bitemporal) o));
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
