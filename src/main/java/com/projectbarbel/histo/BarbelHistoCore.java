package com.projectbarbel.histo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.journal.VersionUpdate;
import com.projectbarbel.histo.journal.VersionUpdate.VersionUpdateResult;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;

public final class BarbelHistoCore implements BarbelHisto {

    private final BarbelHistoContext context;
    private final IndexedCollection<Object> backbone;
    private final Map<Object, DocumentJournal> journals;

    protected BarbelHistoCore(BarbelHistoContext context) {
        this.context = context;
        this.backbone = context.getBackbone();
        this.journals = context.getJournalStore();
    }

    @Override
    public boolean save(Object newVersion, LocalDate from, LocalDate until) {
        Object id = context.getMode().drawDocumentId(newVersion);
        if (journals.containsKey(id)) {
            DocumentJournal journal = journals.get(id);
            Optional<Bitemporal> effectiveVersion = journal.read().effectiveTime().effectiveAt(from);
            if (effectiveVersion.isPresent()) {
                VersionUpdate update = context.getBarbelFactory().createVersionUpdate(effectiveVersion.get())
                        .prepare().effectiveFrom(from).until(until).get();
                VersionUpdateResult result = update.execute();
                result.setNewSubsequentVersion(context.getMode().stampVirgin(context, newVersion,
                        (result.newSubsequentVersion()).getBitemporalStamp()));
                journal.update(context.getBarbelFactory().createJournalUpdateStrategy(), result);
                return true;
            } else
                return straightInsertVirgin(newVersion, from, until, id);
        }
        return straightInsertVirgin(newVersion, from, until, id);
    }

    private boolean straightInsertVirgin(Object currentVersion, LocalDate from, LocalDate until, Object id) {
        BitemporalStamp stamp = BitemporalStamp.of(context.getActivity(), id,
                EffectivePeriod.builder().from(from).until(until).build(),
                RecordPeriod.builder().createdBy(context.getUser()).build());
        journals.put(id, DocumentJournal.create(backbone, id));
        return backbone.add(context.getMode().stampVirgin(context, currentVersion, stamp));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> retrieve(Query<T> query) {
        return (List<T>)backbone.retrieve((Query<Object>)query).stream().collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> List<T> retrieve(Query<T> query, QueryOptions options) {
        return (List<T>)backbone.retrieve((Query<Object>)query, options).stream().collect(Collectors.toList());
    }

    public BarbelHistoContext getContext() {
        return context;
    }

}
