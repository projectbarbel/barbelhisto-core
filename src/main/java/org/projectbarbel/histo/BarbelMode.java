package org.projectbarbel.histo;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;

import net.sf.cglib.proxy.Enhancer;

/**
 * The two modes available in {@link BarbelHisto}. The class contains the modes
 * as well as the mode specific behavior.
 * 
 * @author Niklas Schlimm
 *
 */
public enum BarbelMode implements BarbelModeProcessor {
    POJO {
        @Override
        public Bitemporal snapshotManagedBitemporal(BarbelHistoContext context, Bitemporal pojo,
                BitemporalStamp stamp) {
            Validate.isTrue(pojo instanceof BarbelProxy, "pojo must be instance of BarbelProxy in BarbelMode.POJO");
            Validate.isTrue(Enhancer.isEnhanced(pojo.getClass()), "pojo must be CGLib proxy type in BarbelMode.POJO");
            Object newVersion = context.getPojoCopyFunctionSupplier().get().apply(((BarbelProxy) pojo).getTarget());
            Object newBitemporal = context.getPojoProxyingFunctionSupplier().get().apply(newVersion, stamp);
            return (Bitemporal) newBitemporal;
        }

        @Override
        public Object drawDocumentId(Object pojo) {
            return getIdValue(pojo).orElseThrow(() -> new IllegalArgumentException("document id must not be null"));
        }

        @Override
        public Bitemporal snapshotMaiden(BarbelHistoContext context, Object pojo, BitemporalStamp stamp) {
            Validate.isTrue(!Enhancer.isEnhanced(pojo.getClass()), CGLIB_TYPE_REQUIRED);
            Object copy = context.getPojoCopyFunctionSupplier().get().apply(pojo);
            Object proxy = context.getPojoProxyingFunctionSupplier().get().apply(copy, stamp);
            return (Bitemporal) proxy;
        }

        @Override
        public <T> Collection<Bitemporal> managedBitemporalToCustomPersistenceObjects(Object id,
                IndexedCollection<T> objects) {
            // used on unload, that's why no copy is ok
            return objects.retrieve(BarbelQueries.all(id)).stream().map(
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
                                ((BitemporalVersion<?>) b).getBitemporalStamp()))
                        .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
                return (Collection<T>) output;
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
            return snapshotManagedBitemporal(context, bitemporal, (BitemporalStamp) context
                    .getPojoCopyFunctionSupplier().get().apply(bitemporal.getBitemporalStamp()));
        }

        @Override
        public boolean validateManagedType(BarbelHistoContext context, Object candidate) {
            Validate.isTrue(!candidate.getClass().equals(BitemporalVersion.class),
                    "BitemporalVersion cannot be used in BarbelMode.POJO - set BarbelMode.BITEMPORAL and try again");
            Validate.isTrue(FieldUtils.getFieldsListWithAnnotation(candidate.getClass(), DocumentId.class).size() == 1,
                    "don't forget to add @DocumentId to the document id attribute to the pojo you want to manage");
            return true;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T drawMaiden(BarbelHistoContext context, T object) {
            return (object instanceof BarbelProxy) ? (T) ((BarbelProxy) object).getTarget() : object;
        }

        @Override
        public Class<?> getPersistenceObjectType(Class<?> objectType) {
            return BitemporalVersion.class;
        }

        @Override
        public Bitemporal managedBitemporalToCustomPersistenceObject(Bitemporal bitemporal) {
            return new BitemporalVersion<>(bitemporal.getBitemporalStamp(), ((BarbelProxy) bitemporal).getTarget());
        }

    },
    BITEMPORAL {

        @Override
        public Bitemporal snapshotManagedBitemporal(BarbelHistoContext context, Bitemporal pojo,
                BitemporalStamp stamp) {
            Validate.isTrue(!(pojo instanceof BarbelProxy), "pojo must not be instance of BarbelProxy");
            Validate.isTrue(!Enhancer.isEnhanced(pojo.getClass()), CGLIB_TYPE_REQUIRED);
            Object newVersion = context.getPojoCopyFunctionSupplier().get().apply(pojo);
            ((Bitemporal) newVersion).setBitemporalStamp(stamp);
            return (Bitemporal) newVersion;
        }

        @Override
        public Object drawDocumentId(Object pojo) {
            return getIdValue(pojo).orElseThrow(() -> new IllegalArgumentException("document id must not be null"));
        }

        @Override
        public Bitemporal snapshotMaiden(BarbelHistoContext context, Object pojo, BitemporalStamp stamp) {
            Validate.isTrue(pojo instanceof Bitemporal,
                    "must inherit interface Bitemporal.class when running bitemporal mode");
            Validate.isTrue(!(pojo instanceof BarbelProxy), "pojo must not be instance of BarbelProxy");
            Validate.isTrue(!Enhancer.isEnhanced(pojo.getClass()), CGLIB_TYPE_REQUIRED);
            Object copy = context.getPojoCopyFunctionSupplier().get().apply(pojo);
            ((Bitemporal) copy).setBitemporalStamp(stamp);
            return (Bitemporal) copy;
        }

        @Override
        public <T> Collection<Bitemporal> managedBitemporalToCustomPersistenceObjects(Object id,
                IndexedCollection<T> objects) {
            return objects.retrieve(BarbelQueries.all(id)).stream().map(o -> (Bitemporal) o)
                    .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> Collection<T> customPersistenceObjectsToManagedBitemporals(BarbelHistoContext context,
                Collection<Bitemporal> bitemporals) {
            return (Collection<T>) bitemporals.stream().map(b -> (Object) b)
                    .collect(Collectors.toCollection(ConcurrentIndexedCollection::new));
        }

        @Override
        public Bitemporal copyManagedBitemporal(BarbelHistoContext context, Bitemporal bitemporal) {
            return (Bitemporal) context.getPojoCopyFunctionSupplier().get().apply(bitemporal);
        }

        @Override
        public boolean validateManagedType(BarbelHistoContext context, Object candidate) {
            Validate.isTrue(Bitemporal.class.isAssignableFrom(candidate.getClass()),
                    "don't forget to implement Bitemporal.class interface on the type you want to manage when in mode BarbelMode.BITEMPORAL");
            return true;
        }

        @Override
        public <T> T drawMaiden(BarbelHistoContext context, T object) {
            return object;
        }

        @Override
        public Class<?> getPersistenceObjectType(Class<?> objectType) {
            return objectType;
        }

        @Override
        public Bitemporal managedBitemporalToCustomPersistenceObject(Bitemporal bitemporal) {
            return bitemporal;
        }

    };

    private static final String CGLIB_TYPE_REQUIRED = "pojo must not be CGLib proxy type";

}
