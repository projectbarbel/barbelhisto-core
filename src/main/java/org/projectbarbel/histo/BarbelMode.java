package org.projectbarbel.histo;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.projectbarbel.histo.functions.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;

import net.sf.cglib.proxy.Enhancer;

/**
 * The wording:
 * 
 * - a 'managed bitemporal' is either a proxied pojo or an object implementing {@link Bitemporal}, managed objects are the backbone citizens
 * - 'bitemporal objects' are objects implementing the {@link Bitemporal} interface, as long they don't live in the backbone, they're not considered managed bitemporals
 * - a snapshot always creates a NEW managed bitemporal with a new given {@link BitemporalStamp}
 * - a custom persistent object is always bitemporal object, but not managed
 * 
 * @author niklasschlimm
 *
 */
public abstract class BarbelMode {

    public static BarbelMode POJO = new PojoMode();
    public static BarbelMode BITEMPORAL = new BitemporalMode();

    public abstract <T> T drawMaiden(BarbelHistoContext context, T object);

    public abstract Bitemporal snapshotManagedBitemporal(BarbelHistoContext context, Bitemporal sourceBitemporal,
            BitemporalStamp stamp);

    public abstract Bitemporal snapshotMaiden(BarbelHistoContext context, Object pojo, BitemporalStamp stamp);

    public abstract Bitemporal copyManagedBitemporal(BarbelHistoContext context, Bitemporal bitemporal);

    public abstract Object drawDocumentId(Object pojo);

    public abstract <T> Collection<Bitemporal> managedBitemporalToCustomPersistenceObjects(
            IndexedCollection<T> objects);

    public abstract <T> Collection<T> customPersistenceObjectsToManagedBitemporals(BarbelHistoContext context,
            Collection<Bitemporal> bitemporals);

    public abstract Object fromInternalPersistenceObjectToManagedBitemporal(BarbelHistoContext context,
            BitemporalVersion<?> bv);

    public abstract BitemporalVersion<?> fromManagedBitemporalToInternalPersistenceObject(BarbelHistoContext context,
            Bitemporal bitemporal);

    public abstract boolean validateManagedType(BarbelHistoContext context, Class<?> objectType);

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
            Validate.isTrue(!Enhancer.isEnhanced(pojo.getClass()), "pojo must not be CGI proxy type");
            if (pojo instanceof BarbelProxy)
                pojo = ((BarbelProxy)pojo).getTarget();
            Object copy = context.getPojoCopyFunction().apply(pojo);
            Object proxy = context.getPojoProxyingFunction().apply(copy, stamp);
            return (Bitemporal) proxy;
        }

        @Override
        public <T> Collection<Bitemporal> managedBitemporalToCustomPersistenceObjects(IndexedCollection<T> objects) {
            return objects.stream().map(
                    o -> new BitemporalVersion<>(((Bitemporal) o).getBitemporalStamp(), ((BarbelProxy) o).getTarget()))
                    .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Collection<T> customPersistenceObjectsToManagedBitemporals(BarbelHistoContext context,
                Collection<Bitemporal> bitemporals) {
            try {
                IndexedCollection<Object> output = bitemporals.stream()
                        .map(b -> snapshotMaiden(context, ((BitemporalVersion<?>) b).getObject(),
                                ((BitemporalVersion<?>) b).getStamp()))
                        .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
                return (Collection<T>)output;
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
            return snapshotManagedBitemporal(context, bitemporal,
                    (BitemporalStamp) context.getPojoCopyFunction().apply(bitemporal.getBitemporalStamp()));
        }

        @Override
        public Object fromInternalPersistenceObjectToManagedBitemporal(BarbelHistoContext context,
                BitemporalVersion<?> bv) {
            return context.getPojoProxyingFunction().apply(bv.getObject(), bv.getBitemporalStamp());
        }

        @Override
        public BitemporalVersion<?> fromManagedBitemporalToInternalPersistenceObject(BarbelHistoContext context,
                Bitemporal bitemporal) {
            BitemporalStamp stamp = bitemporal.getBitemporalStamp();
            Object target = ((BarbelProxy) bitemporal).getTarget();
            return new BitemporalVersion<>(stamp, target);
        }

        @Override
        public boolean validateManagedType(BarbelHistoContext context, Class<?> objectType) {
            Validate.isTrue(!objectType.equals(BitemporalVersion.class),
                    "BitemporalVersion cannot be used in BarbelMode.POJO - set BarbelMode.BITEMPORAL and try again");
            Validate.isTrue(FieldUtils.getFieldsListWithAnnotation(objectType, DocumentId.class).size()==1, "don't forget to add @DocumentId to the document id attribute to the pojo you want to manage");
            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T drawMaiden(BarbelHistoContext context, T object) {
            return (object instanceof BarbelProxy) ? (T)((BarbelProxy)object).getTarget() : object;
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
        public <T> Collection<Bitemporal> managedBitemporalToCustomPersistenceObjects(IndexedCollection<T> objects) {
            return objects.stream().map(o -> (Bitemporal) o)
                    .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Collection<T> customPersistenceObjectsToManagedBitemporals(BarbelHistoContext context,
                Collection<Bitemporal> bitemporals) {
            return (Collection<T>)bitemporals.stream().map(b -> (Object) b)
                    .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
        }

        @Override
        public Bitemporal copyManagedBitemporal(BarbelHistoContext context, Bitemporal bitemporal) {
            return (Bitemporal) context.getPojoCopyFunction().apply(bitemporal);
        }

        @Override
        public Object fromInternalPersistenceObjectToManagedBitemporal(BarbelHistoContext context,
                BitemporalVersion<?> bv) {
            return bv.getObject();
        }

        @Override
        public BitemporalVersion<?> fromManagedBitemporalToInternalPersistenceObject(BarbelHistoContext context,
                Bitemporal bitemporal) {
            return new BitemporalVersion<>(bitemporal.getBitemporalStamp(), bitemporal);
        }

        @Override
        public boolean validateManagedType(BarbelHistoContext context, Class<?> objectType) {
            Validate.isTrue(Bitemporal.class.isAssignableFrom(objectType), "don't forget to implement Bitemporal.class interface on the type you want to manage when in mode BarbelMode.BITEMPORAL");
            return true;
       }

        @Override
        public <T> T drawMaiden(BarbelHistoContext context, T object) {
            return object;
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