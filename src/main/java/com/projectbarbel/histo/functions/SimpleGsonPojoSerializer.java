package com.projectbarbel.histo.functions;

import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.cqengine.persistence.support.serialization.PojoSerializer;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalVersion;

public class SimpleGsonPojoSerializer implements PojoSerializer<Bitemporal> {

    private static final String UTF_8 = "UTF-8";

    private Gson gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, BarbelHistoContext.ZDT_DESERIALIZER)
            .registerTypeAdapter(ZonedDateTime.class, BarbelHistoContext.ZDT_SERIALIZER).create();

    private static Map<String, Class<?>> typeMap = new HashMap<>();

    private BarbelHistoContext context;

    public SimpleGsonPojoSerializer(BarbelHistoContext context) {
        this.context = context;
    }

    @Override
    public byte[] serialize(Bitemporal object) {
        if (object instanceof BarbelProxy) { // change persisted type to BitemporalVersion
            Object target = ((BarbelProxy) object).getTarget();
            typeMap.put(target.getClass().getName(), target.getClass());
            object = new BitemporalVersion(((Bitemporal) object).getBitemporalStamp(),
                    ((BarbelProxy) object).getTarget());
        }
        JsonTypeWrapper wrap = new JsonTypeWrapper(object.getClass().getName(), gson.toJson(object));
        byte[] bytes = gson.toJson(wrap).getBytes(Charset.forName(UTF_8));
        return bytes;
    }

    @Override
    public Bitemporal deserialize(byte[] bytes) {
        String json = new String(bytes, Charset.forName(UTF_8));
        JsonTypeWrapper wrap = gson.fromJson(json, JsonTypeWrapper.class);
        Object object = gson.fromJson(wrap.json, typeMap.computeIfAbsent(wrap.type, computeIfAbsent()));
        if (object instanceof BitemporalVersion) {
            BitemporalVersion bv = (BitemporalVersion) object;
            Bitemporal bitemporal = (Bitemporal) context.getMode().snapshotMaiden(context,
                    gson.fromJson(gson.toJsonTree(bv.getObject()).toString(), typeMap.computeIfAbsent(bv.getObjectType(), computeIfAbsent())),
                    bv.getStamp());
            return bitemporal;
        }
        return (Bitemporal) object;
    }

    private Function<? super String, ? extends Class<?>> computeIfAbsent() {
        return (k)->{
            try {
                return Class.forName(k);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("failed to deserialize type from persistence", e);
            }
        };
    }

    public static class JsonTypeWrapper {
        public String type;
        public String json;

        public JsonTypeWrapper(String type, String json) {
            super();
            this.type = type;
            this.json = json;
        }
    }
}
