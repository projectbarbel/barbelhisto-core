package com.projectbarbel.histo;

import static com.googlecode.cqengine.query.QueryFactory.descending;
import static com.googlecode.cqengine.query.QueryFactory.equal;
import static com.googlecode.cqengine.query.QueryFactory.orderBy;
import static com.googlecode.cqengine.query.QueryFactory.queryOptions;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.journal.functions.JournalUpdateStrategyEmbedding.JournalUpdateCase;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;

public final class BarbelHistoCore implements BarbelHisto {

    private final BarbelHistoContext context;
    private final IndexedCollection<Object> backbone;
    private final Map<Object, DocumentJournal> journals;
    private final IndexedCollection<UpdateLogRecord> updateLog;

    protected BarbelHistoCore(BarbelHistoContext context) {
        this.context = context;
        this.backbone = context.getBackbone();
        this.journals = context.getJournalStore();
        this.updateLog = context.getUpdateLog();
    }

    @Override
    public boolean save(Object newVersion, LocalDate from, LocalDate until) {
        Validate.isTrue(newVersion!=null&&from!=null&&until!=null, "all arguments must not be null here");
        Object id = context.getMode().drawDocumentId(newVersion);
        BitemporalStamp stamp = BitemporalStamp.of(context.getActivity(), id, EffectivePeriod.of(from, until),
                RecordPeriod.createActive(context));
        DocumentJournal journal = journals.computeIfAbsent(id, (k)->DocumentJournal.create(backbone, k));
        // request lock für diese Document ID im backbone für diesen User! If fails
        // return AlreadyLockedException
        // TODO: ab hier sperren? Das komplette backbone für diese ID sperren?
        Bitemporal newManagedBitemporal = context.getMode().snapshotMaiden(context, newVersion, stamp);
        BiConsumer<DocumentJournal, Bitemporal> updateStrategy = context.getBarbelFactory()
                .createJournalUpdateStrategy();
        updateStrategy.accept(journal, newManagedBitemporal);
        updateLog.add(new UpdateLogRecord(journal.getLastInsert(), newManagedBitemporal,
                updateStrategy instanceof UpdateCaseAware ? ((UpdateCaseAware) updateStrategy).getActualCase() : null,
                context.getUser()));
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> retrieve(Query<T> query) {
        return (List<T>) backbone.retrieve((Query<Object>) query).stream().collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> retrieve(Query<T> query, QueryOptions options) {
        return (List<T>) backbone.retrieve((Query<Object>) query, options).stream().collect(Collectors.toList());
    }

    @Override
    public String prettyPrintJournal(Object id, Function<Bitemporal, String> customField) {
        if (journals.containsKey(id))
            return DocumentJournal.prettyPrint(journals.get(id).collection(), id, customField);
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

}
