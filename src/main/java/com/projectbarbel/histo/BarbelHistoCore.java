package com.projectbarbel.histo;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.AnnotationUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;

import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.api.DocumentJournal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.BitemporalVersion;
import com.projectbarbel.histo.model.EffectivePeriod;

public class BarbelHistoCore implements BarbelHisto {

    private final BarbelHistoContext context;
    private IndexedCollection<BitemporalVersion> backbone;
    private Map<Object, DocumentJournal<BitemporalVersion>> journals;
    private String activity;

    protected BarbelHistoCore(BarbelHistoContext context) {
        this.context = context;
        this.backbone = context.getBackbone();
        this.activity = context.getActivity();
    }

    @Override
    public void save(Object currentVersion, LocalDate from, LocalDate until) {
        List<Field> fields = FieldUtils.getFieldsListWithAnnotation(currentVersion.getClass(), DocumentId.class);
        Validate.isTrue(fields.size() == 1,
                "cannot find document id - make sure exactly one field in the pojo is annotated with @DocumentId");
        Object id = fields.get(0).get(currentVersion);
        if (!journals.containsKey(id)) {
            journals.put(id, DocumentJournal.create(backbone));
        }
        BitemporalStamp stamp = BitemporalStamp.of(activity, id, null, null);

        BitemporalVersion version = new BitemporalVersion(BitemporalStamp.builder().withEffectiveTime(period).build(),
                currentVersion);
        backbone.add(version);

    }

    public BarbelHistoContext getContext() {
        return context;
    }

}
