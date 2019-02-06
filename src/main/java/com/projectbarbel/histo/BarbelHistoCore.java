package com.projectbarbel.histo;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;

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

public final class BarbelHistoCore<T> implements BarbelHisto<T> {

    private static final String DOCUMENT_ID_MUST_NOT_BE_NULL = "document id must not be null";
    private final BarbelHistoContext<T> context;
    private final IndexedCollection<T> backbone;
    private final Map<Object, DocumentJournal<T>> journals;

    protected BarbelHistoCore(BarbelHistoContext<T> context) {
        this.context = context;
        this.backbone = context.getBackbone();
        this.journals = context.getJournalStore();
    }

    @Override
    public boolean save(T newVersion, LocalDate from, LocalDate until) {
        Object id = getIdValue(newVersion)
                .orElseThrow(() -> new IllegalArgumentException(DOCUMENT_ID_MUST_NOT_BE_NULL));
        if (journals.containsKey(id)) {
            DocumentJournal<T> journal = journals.get(id);
            Optional<T> effectiveVersion = journal.read().effectiveTime().effectiveAt(from);
            if (effectiveVersion.isPresent()) {
                VersionUpdate<T> update = context.getBarbelFactory().createVersionUpdate(effectiveVersion.get())
                        .prepare().effectiveFrom(from).until(until).get();
                VersionUpdateResult<T> result = update.execute();
                result.setNewSubsequentVersion(context.getMode().stampVirgin(context, newVersion,
                        ((Bitemporal) result.newSubsequentVersion()).getBitemporalStamp()));
                journal.update(context.getBarbelFactory().createJournalUpdateStrategy(), result);
                return true;
            } else
                return doSaveInitial(newVersion, from, until, id);
        }
        return doSaveInitial(newVersion, from, until, id);
    }

    private boolean doSaveInitial(T currentVersion, LocalDate from, LocalDate until, Object id) {
        BitemporalStamp stamp = BitemporalStamp.of(context.getActivity(), id,
                EffectivePeriod.builder().from(from).until(until).build(),
                RecordPeriod.builder().createdBy(context.getUser()).build());
        journals.put(id, DocumentJournal.create((IndexedCollection<T>) backbone, id));
        T copy = context.getPojoCopyFunction().apply(currentVersion);
        T proxy = context.getPojoProxyingFunction().apply(copy, stamp);
        return backbone.add(proxy);
    }

    @Override
    public List<T> retrieve(Query<T> query) {
        return backbone.retrieve(query).stream().collect(Collectors.toList());
    }

    @Override
    public List<T> retrieve(Query<T> query, QueryOptions options) {
        return backbone.retrieve(query, options).stream().collect(Collectors.toList());
    }

    protected Optional<Object> getIdValue(T currentVersion) {
        List<Field> fields = FieldUtils.getFieldsListWithAnnotation(currentVersion.getClass(), DocumentId.class);
        Validate.isTrue(fields.size() == 1,
                "cannot find document id - make sure exactly one field in the pojo is annotated with @DocumentId");
        fields.get(0).setAccessible(true);
        try {
            return Optional.ofNullable(fields.get(0).get(currentVersion));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "wrong parameters passed to field accessor when retrieving document id on class: "
                            + currentVersion.getClass().getName(),
                    e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("no access permission when trying to receive document id on class: "
                    + currentVersion.getClass().getName(), e);
        }
    }

    public BarbelHistoContext<T> getContext() {
        return context;
    }

}
