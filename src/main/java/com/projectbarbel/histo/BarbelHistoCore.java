package com.projectbarbel.histo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.journal.DocumentJournal;
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
        BitemporalStamp stamp = BitemporalStamp.of(context.getActivity(), id,
                EffectivePeriod.of(from,until),
                RecordPeriod.builder().createdBy(context.getUser()).build());
        if (journals.containsKey(id)) {
            DocumentJournal journal = journals.get(id);
            Optional<Bitemporal> effectiveVersion = journal.read().effectiveTime().effectiveAt(from);
            if (effectiveVersion.isPresent()) {
                context.getBarbelFactory().createJournalUpdateStrategy().accept(journal, context.getMode().snapshotMaiden(context, newVersion, stamp));
                return true;
            } else
                return straightInsert(newVersion, stamp, id);
        }
        return straightInsert(newVersion, stamp, id);
    }

    private boolean straightInsert(Object currentVersion, BitemporalStamp stamp, Object id) {
        journals.put(id, DocumentJournal.create(backbone, id));
        return backbone.add(context.getMode().snapshotMaiden(context, currentVersion, stamp));
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
    
    @Override
    public String prettyPrintJournal(Object id, Function<Bitemporal, String> customField) {
        if (journals.containsKey(id))
            return DocumentJournal.prettyPrint(journals.get(id).collection(), id,customField);
        else
            return "";
    }

    public BarbelHistoContext getContext() {
        return context;
    }

}
