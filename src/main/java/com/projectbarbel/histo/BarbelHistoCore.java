package com.projectbarbel.histo;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;

import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.projectbarbel.histo.journal.DocumentJournal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.BitemporalVersion;
import com.projectbarbel.histo.model.EffectivePeriod;
import com.projectbarbel.histo.model.RecordPeriod;

public class BarbelHistoCore implements BarbelHisto {

    private final BarbelHistoContext context;
    private final IndexedCollection<BitemporalVersion> backbone;
    private final Map<Object, DocumentJournal<BitemporalVersion>> journals;
    private final String activity;
    private final String user;

    public static Attribute<BitemporalVersion, ChronoLocalDate> BV_EFFECTIVE_DATE() {
        return new SimpleAttribute<BitemporalVersion, ChronoLocalDate>("effectiveDate") {

            @Override
            public ChronoLocalDate getValue(BitemporalVersion object, QueryOptions queryOptions) {
                return object.getBitemporalStamp().getEffectiveTime().from();
            }

        };
    }

    protected BarbelHistoCore(BarbelHistoContext context) {
        this.context = context;
        this.backbone = context.getBackbone();
        this.activity = context.getActivity();
        this.user = context.getUser();
        this.journals = context.getJournalStore();
    }

    @Override
    public void save(Object currentVersion, LocalDate from, LocalDate until) {
        Optional<Object> id = getIdValue(currentVersion);
        doSaveInitial(currentVersion, from, until,
                id.orElseThrow(() -> new IllegalArgumentException("document id must not be null")));
    }

    private void doSaveInitial(Object currentVersion, LocalDate from, LocalDate until, Object id) {
        BitemporalStamp stamp = BitemporalStamp.of(activity, (String) id,
                EffectivePeriod.builder().from(from).until(until).build(),
                RecordPeriod.builder().createdBy(user).build());
        BitemporalVersion version = new BitemporalVersion(stamp, currentVersion);
        journals.put(id, DocumentJournal.create(backbone, id));
        backbone.add(version);
    }

    public List<Object> retrieve(Object id, Function<Object, Query<BitemporalVersion>> queryFunction) {
        return backbone.retrieve(queryFunction.apply(id)).stream().collect(Collectors.toList());
    }

    protected Optional<Object> getIdValue(Object currentVersion) {
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

    public BarbelHistoContext getContext() {
        return context;
    }

}
