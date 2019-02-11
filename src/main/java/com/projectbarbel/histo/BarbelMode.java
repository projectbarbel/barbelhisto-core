package com.projectbarbel.histo;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.projectbarbel.histo.functions.BarbelProxy;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;
import com.projectbarbel.histo.model.BitemporalVersion;

import net.sf.cglib.proxy.Enhancer;

public abstract class BarbelMode {

    public static BarbelMode POJO = new PojoMode();
    public static BarbelMode BITEMPORAL = new BitemporalMode();

    public abstract Bitemporal snapshotManagedBitemporal(BarbelHistoContext context, Bitemporal sourceBitemporal,
            BitemporalStamp stamp);

    public abstract Bitemporal snapshotMaiden(BarbelHistoContext context, Object pojo, BitemporalStamp stamp);

    public abstract Bitemporal copyManagedBitemporal(BarbelHistoContext context, Bitemporal bitemporal);

    public abstract Object drawDocumentId(Object pojo);

    public abstract Collection<Bitemporal> managedObjectsToBitemporals(IndexedCollection<Object> objects);

    public abstract Collection<Object> populateBitemporals(BarbelHistoContext context,
            Collection<Bitemporal> bitemporals);

    public static class PojoMode extends BarbelMode {

        @Override
        public Bitemporal snapshotManagedBitemporal(BarbelHistoContext context, Bitemporal pojo,
                BitemporalStamp stamp) {
            Validate.isTrue(pojo instanceof BarbelProxy, "pojo must be instance of BarbelProxy");
            Validate.isTrue(Enhancer.isEnhanced(pojo.getClass()), "pojo must be CGI proxy type");
            Object newVersion = context.getPojoCopyFunction().apply(((BarbelProxy) pojo).getTarget());
            if (newVersion instanceof Bitemporal) { // make sure target and proxy will always sync their stamps
                ((Bitemporal) newVersion).setBitemporalStamp(stamp);
            }
            Object newBitemporal = context.getPojoProxyingFunction().apply(newVersion, stamp);
            return (Bitemporal) newBitemporal;
        }

        @Override
        public Object drawDocumentId(Object pojo) {
            return getIdValue(pojo).orElseThrow(() -> new IllegalArgumentException("document id must not be null"));
        }

        @Override
        public Bitemporal snapshotMaiden(BarbelHistoContext context, Object pojo, BitemporalStamp stamp) {
            Validate.isTrue(!(pojo instanceof BarbelProxy), "pojo must not be instance of BarbelProxy");
            Validate.isTrue(!Enhancer.isEnhanced(pojo.getClass()), "pojo must not be CGI proxy type");
            Object copy = context.getPojoCopyFunction().apply(pojo);
            Object proxy = context.getPojoProxyingFunction().apply(copy, stamp);
            return (Bitemporal) proxy;
        }

        @Override
        public Collection<Bitemporal> managedObjectsToBitemporals(IndexedCollection<Object> objects) {
            return objects.stream().map(
                    o -> new BitemporalVersion(((Bitemporal) o).getBitemporalStamp(), ((BarbelProxy) o).getTarget()))
                    .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
        }

        @Override
        public Collection<Object> populateBitemporals(BarbelHistoContext context, Collection<Bitemporal> bitemporals) {
            try {
                IndexedCollection<Object> output = bitemporals.stream()
                        .map(b -> snapshotMaiden(context, ((BitemporalVersion) b).getObject(),
                                ((BitemporalVersion) b).getStamp()))
                        .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
                return output;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException(
                        "Only BitemporalVersion type collection allowed. Use Mode Bitemporal if you want to use populate() with arbitrary Bitemporal objects. Or use save() to store plain Pojos to BarbelHisto.",
                        e);
            }
        }

        @Override
        public String toString() {
            return "PojoMode";
        }

        @Override
        public Bitemporal copyManagedBitemporal(BarbelHistoContext context, Bitemporal bitemporal) {
            return snapshotManagedBitemporal(context, bitemporal, bitemporal.getBitemporalStamp());
        }

    }

    public static class BitemporalMode extends BarbelMode {

        @Override
        public Bitemporal snapshotManagedBitemporal(BarbelHistoContext context, Bitemporal pojo,
                BitemporalStamp stamp) {
            Validate.isTrue(!(pojo instanceof BarbelProxy), "pojo must not be instance of BarbelProxy");
            Validate.isTrue(!Enhancer.isEnhanced(pojo.getClass()), "pojo must not be CGI proxy type");
            Object newVersion = context.getPojoCopyFunction().apply(pojo);
            ((Bitemporal) newVersion).setBitemporalStamp(stamp);
            return (Bitemporal) newVersion;
        }

        @Override
        public Object drawDocumentId(Object pojo) {
            return ((Bitemporal) pojo).getBitemporalStamp().getDocumentId();
        }

        @Override
        public Bitemporal snapshotMaiden(BarbelHistoContext context, Object pojo, BitemporalStamp stamp) {
            Validate.isTrue(pojo instanceof Bitemporal,
                    "must inherit interface Bitemporal.class when running bitemporal mode");
            Validate.isTrue(!(pojo instanceof BarbelProxy), "pojo must not be instance of BarbelProxy");
            Validate.isTrue(!Enhancer.isEnhanced(pojo.getClass()), "pojo must not be CGI proxy type");
            Object copy = context.getPojoCopyFunction().apply(pojo);
            ((Bitemporal) copy).setBitemporalStamp(stamp);
            return (Bitemporal) copy;
        }

        @Override
        public Collection<Bitemporal> managedObjectsToBitemporals(IndexedCollection<Object> objects) {
            return objects.stream().map(o -> (Bitemporal) o)
                    .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
        }

        @Override
        public Collection<Object> populateBitemporals(BarbelHistoContext context, Collection<Bitemporal> bitemporals) {
            return bitemporals.stream().map(b -> (Object) b)
                    .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
        }

        @Override
        public Bitemporal copyManagedBitemporal(BarbelHistoContext context, Bitemporal bitemporal) {
            return (Bitemporal) context.getPojoCopyFunction().apply(bitemporal);
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