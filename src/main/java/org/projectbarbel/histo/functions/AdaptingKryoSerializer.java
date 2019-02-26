package org.projectbarbel.histo.functions;

import java.util.Objects;
import java.util.Optional;

import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;

import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.persistence.offheap.OffHeapPersistence;
import com.googlecode.cqengine.persistence.support.serialization.KryoSerializer;
import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;
import com.googlecode.cqengine.persistence.support.serialization.PojoSerializer;

/**
 * Serializer that is used in case clients decide to use {@link DiskPersistence}
 * and {@link OffHeapPersistence} on their backbone collection. Uses the
 * original cqengine {@link KryoSerializer} but handles the case when proxies
 * are passed for persistence. Proxies will always be persisted as
 * {@link BitemporalVersion} instead of the dynamic class created by CGLib or
 * any other. Notice that proxies need to implement {@link BarbelProxy} and
 * {@link Bitemporal} so that they can be managed here.
 * 
 * @author Niklas Schlimm
 *
 */
public class AdaptingKryoSerializer implements PojoSerializer<Bitemporal> {

    private final KryoSerializer<Bitemporal> targetKryo;
    private final BarbelHistoContext context;
    private final Class<?> objectType;
    private final PersistenceConfig persistenceConfig;
    public static final String OBJECT_TYPE = "objectType";
    public static final String PERSISTENCE_CONFIG = "persistenceConfig";

    public AdaptingKryoSerializer(BarbelHistoContext context) {
        super();
        this.context = Objects.requireNonNull(context, "the context must not be null");
        objectType = Optional.ofNullable((Class<?>) context.getContextOptions().get(OBJECT_TYPE))
                .orElseThrow(() -> new IllegalStateException("could not find objectType"));
        persistenceConfig = Optional.ofNullable((PersistenceConfig) context.getContextOptions().get(PERSISTENCE_CONFIG))
                .orElseThrow(() -> new IllegalStateException("could not find persistenceConfig"));
        @SuppressWarnings("unchecked")
        KryoSerializer<Bitemporal> kryo = new KryoSerializer<>(
                (Class<Bitemporal>) context.getMode().getPersistenceObjectType(objectType), persistenceConfig);
        this.targetKryo = kryo;
    }

    @Override
    public byte[] serialize(final Bitemporal object) {
        if (object instanceof BarbelProxy) { // change persisted type to BitemporalVersion
            return targetKryo.serialize(new BitemporalVersion((object).getBitemporalStamp(),
                    ((BarbelProxy) object).getTarget()));
        }
        return targetKryo.serialize(object);
    }

    @Override
    public Bitemporal deserialize(byte[] bytes) {
        Bitemporal bitemporal = targetKryo.deserialize(bytes);
        if (bitemporal instanceof BitemporalVersion) {
            BitemporalVersion bv = (BitemporalVersion) bitemporal;
            Object bvobject = bv.getObject();
            if (context.getMode() == BarbelMode.POJO)
                return context.getMode().snapshotMaiden(context, bvobject, bv.getBitemporalStamp());
            else
                return new BitemporalVersion(bv.getBitemporalStamp(), bvobject);
        }
        return bitemporal;
    }

    public static <O> boolean validateObjectIsRoundTripSerializable(BarbelHistoContext context, O candidatePojo) {
        try {
            context.getContextOptions().put(OBJECT_TYPE, candidatePojo.getClass());
            context.getContextOptions().put(PERSISTENCE_CONFIG,
                    Optional.ofNullable(candidatePojo.getClass().getAnnotation(PersistenceConfig.class))
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "missing @PersistenceConfig annotation on candidate POJO")));
            AdaptingKryoSerializer serializer = new AdaptingKryoSerializer(context);
            if (candidatePojo instanceof Bitemporal) {
                byte[] bytes = serializer.serialize((Bitemporal) candidatePojo);
                Bitemporal bitemporal = serializer.deserialize(bytes);
                validateHashCodeEquality(candidatePojo, bitemporal);
                validateObjectEquality(candidatePojo, bitemporal);
            } else {
                Bitemporal proxy = context.getMode().snapshotMaiden(context, candidatePojo,
                        BitemporalStamp.createActive());
                byte[] bytes = serializer.serialize(proxy);
                Bitemporal bitemporal = serializer.deserialize(bytes);
                validateHashCodeEquality(candidatePojo, ((BarbelProxy) bitemporal).getTarget());
                validateObjectEquality(candidatePojo, ((BarbelProxy) bitemporal).getTarget());
            }
            return true;
        } catch (Exception e) {
            throw new IllegalStateException(
                    "POJO object failed round trip serialization-deserialization test, object type: "
                            + (candidatePojo == null ? "null" : candidatePojo.getClass()) + ", object: " + candidatePojo,
                    e);
        }
    }

    static void validateObjectEquality(Object candidate, Object deserializedPojo) {
        if (!(deserializedPojo.equals(candidate))) {
            throw new IllegalStateException(
                    "The POJO after round trip serialization is not equal to the original POJO - not implemented equals()?");
        }
    }

    static void validateHashCodeEquality(Object candidate, Object deserializedPojo) {
        if (deserializedPojo.hashCode() != candidate.hashCode()) {
            throw new IllegalStateException(
                    "The POJO's hashCode after round trip serialization differs from its original hashCode - not implemented hashCode()?");
        }
    }

}
