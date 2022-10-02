package org.projectbarbel.histo.functions;

import java.lang.reflect.Type;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.model.BarbelProxy;
import org.projectbarbel.histo.model.Bitemporal;
import org.projectbarbel.histo.model.BitemporalStamp;
import org.projectbarbel.histo.model.BitemporalVersion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;
import com.googlecode.cqengine.persistence.support.serialization.PojoSerializer;

/**
 * 
 * @author Niklas Schlimm
 *
 */
public class AdaptingGsonSerializer implements PojoSerializer<Bitemporal> {

    private final Gson gson;
    private final BarbelHistoContext context;
    private final Class<? extends Bitemporal> objectType;
    @SuppressWarnings("unused")
    private final PersistenceConfig persistenceConfig;
    public static final String OBJECT_TYPE = "objectType";
    public static final String PERSISTENCE_CONFIG = "persistenceConfig";

    @SuppressWarnings("unchecked")
    public AdaptingGsonSerializer(BarbelHistoContext context) {
        super();
        this.context = Objects.requireNonNull(context, "the context must not be null");
        final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        objectType = Optional.ofNullable((Class<? extends Bitemporal>) context.getContextOptions().get(OBJECT_TYPE))
                .orElseThrow(() -> new IllegalStateException("could not find objectType"));
        persistenceConfig = Optional.ofNullable((PersistenceConfig) context.getContextOptions().get(PERSISTENCE_CONFIG))
                .orElseThrow(() -> new IllegalStateException("could not find persistenceConfig"));
        gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new JsonDeserializer<ZonedDateTime>() {

            @Override
            public ZonedDateTime deserialize(JsonElement json, Type type,
                    JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                return formatter.parse(json.getAsString(), ZonedDateTime::from);
            }
        }).registerTypeAdapter(ZonedDateTime.class, new JsonSerializer<ZonedDateTime>() {

            @Override
            public JsonElement serialize(ZonedDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(formatter.format(src));
            }
        }).create();
    }

    @Override
    public byte[] serialize(final Bitemporal object) {
        if (object instanceof BarbelProxy) { // change persisted type to BitemporalVersion
            return gson.toJson(new BitemporalVersion((object).getBitemporalStamp(), ((BarbelProxy) object).getTarget()))
                    .getBytes();
        }
        return gson.toJson(object).getBytes();
    }

    @Override
    public Bitemporal deserialize(byte[] bytes) {
        Bitemporal bitemporal = null;
        try {
            bitemporal = gson.fromJson(new String(bytes), objectType);
        } catch (ClassCastException e) {
            bitemporal = gson.fromJson(new String(bytes), BitemporalVersion.class);
        }
        if (bitemporal instanceof BitemporalVersion) {
            BitemporalVersion bv = (BitemporalVersion) bitemporal;
            bv.setObject(gson.fromJson(gson.toJsonTree(bv.getObject()).getAsJsonObject(), objectType));
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
            AdaptingGsonSerializer serializer = new AdaptingGsonSerializer(context);
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
                            + (candidatePojo == null ? "null" : candidatePojo.getClass()) + ", object: "
                            + candidatePojo,
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
