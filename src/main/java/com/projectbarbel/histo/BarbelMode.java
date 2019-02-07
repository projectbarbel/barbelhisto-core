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

    public abstract Bitemporal stampPojo(BarbelHistoContext context, Object newVersion, BitemporalStamp stamp);

    public abstract Bitemporal snapshotManagedBitemporal(BarbelHistoContext context, Bitemporal sourceBitemporal, BitemporalStamp stamp);

    public abstract Bitemporal snapshotPojo(BarbelHistoContext context, Object pojo, BitemporalStamp stamp);
    
    public abstract Object drawDocumentId(Object pojo);

    public static class PojoMode extends BarbelMode {

        @Override
        public Bitemporal stampPojo(BarbelHistoContext context, Object newVersion, BitemporalStamp stamp) {
            return (Bitemporal)context.getPojoProxyingFunction().apply(newVersion, stamp);
        }

        @Override
        public Bitemporal snapshotManagedBitemporal(BarbelHistoContext context, Bitemporal pojo, BitemporalStamp stamp) {
            Object newVersion = context.getPojoCopyFunction().apply(((BarbelProxy) pojo).getTarget());
            if (newVersion instanceof Bitemporal) { // make sure target and proxy will always sync their stamps
                ((Bitemporal) newVersion).setBitemporalStamp(stamp);
            }
            Object newBitemporal = context.getPojoProxyingFunction().apply(newVersion, stamp);
            return (Bitemporal)newBitemporal;
        }

        @Override
        public Object drawDocumentId(Object pojo) {
            return getIdValue(pojo)
                    .orElseThrow(() -> new IllegalArgumentException("document id must not be null"));
        }

        @Override
        public Bitemporal snapshotPojo(BarbelHistoContext context, Object pojo, BitemporalStamp stamp) {
            Object copy = context.getPojoCopyFunction().apply(pojo);
            Object proxy = context.getPojoProxyingFunction().apply(copy, stamp);
            return (Bitemporal)proxy;
        }

    }

    public static class BitemporalMode extends BarbelMode {

        @Override
        public Bitemporal stampPojo(BarbelHistoContext context, Object newVersion, BitemporalStamp stamp) {
            ((BitemporalVersion) newVersion).setBitemporalStamp(stamp);
            return (Bitemporal)newVersion;
        }

        @Override
        public Bitemporal snapshotManagedBitemporal(BarbelHistoContext context, Bitemporal pojo, BitemporalStamp stamp) {
            Object newVersion = context.getPojoCopyFunction().apply(pojo);
            ((Bitemporal)newVersion).setBitemporalStamp(stamp);
            return (Bitemporal)newVersion;
        }

        @Override
        public Object drawDocumentId(Object pojo) {
            return ((Bitemporal)pojo).getBitemporalStamp().getDocumentId();
        }

        @Override
        public Bitemporal snapshotPojo(BarbelHistoContext context, Object pojo, BitemporalStamp stamp) {
            Object copy = context.getPojoCopyFunction().apply(pojo);
            ((Bitemporal)copy).setBitemporalStamp(stamp);
            return (Bitemporal)copy;
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