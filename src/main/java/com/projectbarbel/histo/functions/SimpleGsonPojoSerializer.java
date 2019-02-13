package com.projectbarbel.histo.functions;

import java.nio.charset.Charset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.googlecode.cqengine.persistence.support.serialization.PojoSerializer;
import com.projectbarbel.histo.BarbelHistoBuilder;
import com.projectbarbel.histo.BarbelHistoContext;
import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalVersion;

public class SimpleGsonPojoSerializer implements PojoSerializer<Bitemporal> {

    private static final String UTF_8 = "UTF-8";

    private Gson gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, BarbelHistoContext.ZDT_DESERIALIZER)
            .registerTypeAdapter(ZonedDateTime.class, BarbelHistoContext.ZDT_SERIALIZER).create();

    private Map<String, Class<?>> typeMap = new HashMap<>();

    @Override
    public byte[] serialize(Bitemporal object) {
        if (object instanceof BarbelProxy) { // change persisted type to BitemporalVersion
            Object target = ((BarbelProxy) object).getTarget();
            typeMap.putIfAbsent(target.getClass().getName(), target.getClass());
            object = new BitemporalVersion(((Bitemporal) object).getBitemporalStamp(),
                    ((BarbelProxy) object).getTarget());
        }
        typeMap.putIfAbsent(object.getClass().getName(), object.getClass());
        JsonTypeWrapper wrap = new JsonTypeWrapper(object.getClass().getName(), gson.toJson(object));
        byte[] bytes = gson.toJson(wrap).getBytes(Charset.forName(UTF_8));
        return bytes;
    }

    @Override
    public Bitemporal deserialize(byte[] bytes) {
        String json = new String(bytes, Charset.forName(UTF_8));
        JsonTypeWrapper wrap = gson.fromJson(json, JsonTypeWrapper.class);
        Object object = gson.fromJson(wrap.json, typeMap.get(wrap.type));
        if (object instanceof BitemporalVersion) {
            BitemporalVersion bv = (BitemporalVersion) object;
            Bitemporal bitemporal = (Bitemporal) BarbelHistoBuilder.getPersistencePojoProxyingFunction().apply(
                    gson.fromJson(gson.toJsonTree(bv.getObject()).toString(), typeMap.get(bv.getObjectType())), bv.getStamp());
            return bitemporal;
        }
        return (Bitemporal) object;
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
