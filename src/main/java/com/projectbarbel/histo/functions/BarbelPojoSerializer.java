package com.projectbarbel.histo.functions;

import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;
import com.googlecode.cqengine.persistence.support.serialization.PojoSerializer;
import com.projectbarbel.histo.BarbelHistoBuilder;
import com.projectbarbel.histo.model.Bitemporal;

public class BarbelPojoSerializer<O> implements PojoSerializer<O> {

    private Class<O> type;
    private PersistenceConfig config;
    
    public BarbelPojoSerializer(Class<O> type, PersistenceConfig config) {
        super();
        this.type = type;
        this.config = config;
    }

    @Override
    public byte[] serialize(O object) {
        return BarbelHistoBuilder.getPersistenceSerializerSingleton().serialize((Bitemporal)object);
    }

    @SuppressWarnings("unchecked")
    @Override
    public O deserialize(byte[] bytes) {
        return (O)BarbelHistoBuilder.getPersistenceSerializerSingleton().deserialize(bytes);
    }

    public Class<O> getType() {
        return type;
    }

    public PersistenceConfig getConfig() {
        return config;
    }

}
