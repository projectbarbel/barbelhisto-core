package org.projectbarbel.histo.functions;

import java.util.Optional;

import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.model.Bitemporal;

import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;
import com.googlecode.cqengine.persistence.support.serialization.PojoSerializer;

public class BarbelPojoSerializer<O> implements PojoSerializer<O> {

    private Class<O> type;
    private PersistenceConfig config;
    private PojoSerializer<Bitemporal> targetSerializer;

    public BarbelPojoSerializer(Class<O> type, PersistenceConfig config) {
        super();
        this.type = type;
        this.config = config;
        this.targetSerializer = Optional.ofNullable(BarbelHistoCore.CONSTRUCTION_CONTEXT.get())
                .orElseGet(() -> BarbelHistoBuilder.barbel()).getPersistenceSerializerProducer()
                .apply(BarbelHistoCore.CONSTRUCTION_CONTEXT.get());
    }

    @Override
    public byte[] serialize(O object) {
        return targetSerializer.serialize((Bitemporal) object);
    }

    @SuppressWarnings("unchecked")
    @Override
    public O deserialize(byte[] bytes) {
        return (O) targetSerializer.deserialize(bytes);
    }

    public Class<O> getType() {
        return type;
    }

    public PersistenceConfig getConfig() {
        return config;
    }

}
