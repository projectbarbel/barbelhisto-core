package com.projectbarbel.histo;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;

import com.projectbarbel.histo.journal.functions.BarbelProxy;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.BitemporalVersion;

public abstract class BarbelMode {

    public static BarbelMode POJO = new PojoMode();
    public static BarbelMode BITEMPORAL = new BitemporalMode();

    public abstract <T> T stampVirgin(BarbelHistoContext<T> context, T newVersion, BitemporalStamp stamp);

    public abstract <T> T copy(BarbelHistoContext<T> context, T pojo);
    
    public abstract <T> Object drawDocumentId(T pojo);

    public static class PojoMode extends BarbelMode {

        @Override
        public <T> T stampVirgin(BarbelHistoContext<T> context, T newVersion, BitemporalStamp stamp) {
            return context.getPojoProxyingFunction().apply(newVersion, stamp);
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T copy(BarbelHistoContext<T> context, T pojo) {
            return context.getPojoCopyFunction().apply(((BarbelProxy<T>) pojo).getTarget());
        }

        @Override
        public <T> Object drawDocumentId(T pojo) {
            return getIdValue(pojo)
                    .orElseThrow(() -> new IllegalArgumentException("document id must not be null"));
        }

    }

    public static class BitemporalMode extends BarbelMode {

        @Override
        public <T> T stampVirgin(BarbelHistoContext<T> context, T newVersion, BitemporalStamp stamp) {
            ((BitemporalVersion) newVersion).setBitemporalStamp(stamp);
            return newVersion;
        }

        @Override
        public <T> T copy(BarbelHistoContext<T> context, T pojo) {
            return context.getPojoCopyFunction().apply(pojo);
        }

        @Override
        public <T> Object drawDocumentId(T pojo) {
            return ((Bitemporal)pojo).getBitemporalStamp().getDocumentId();
        }

    }
    
    protected static <T> Optional<Object> getIdValue(T currentVersion) {
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


}