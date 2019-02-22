package org.projectbarbel.histo.functions;

import java.util.Optional;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelHistoBuilder;
import org.projectbarbel.histo.BarbelHistoContext;
import org.projectbarbel.histo.BarbelHistoCore;
import org.projectbarbel.histo.model.Bitemporal;

import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.persistence.offheap.OffHeapPersistence;
import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;
import com.googlecode.cqengine.persistence.support.serialization.PojoSerializer;

/**
 * The forwarding serializer always used by {@link BarbelHisto}. If clients
 * decide to use {@link DiskPersistence} or {@link OffHeapPersistence} they need
 * to add the {@link PersistenceConfig} annotation additionally to their
 * business classes. <br>
 * <br>
 * 
 * <pre>
 * <code>@PersistenceConfig(serializer=BarbelPojoSerializer.class, polymorphic=true)</code>
 * </pre>
 * 
 * 
 * @author Niklas Schlimm
 *
 * @param <O> the stored type
 */
public class BarbelPojoSerializer<O> implements PojoSerializer<O> {

    private Class<O> type;
    private PersistenceConfig config;
    private PojoSerializer<Bitemporal> targetSerializer;

    public BarbelPojoSerializer(Class<O> type, PersistenceConfig config) {
        super();
        this.type = type;
        this.config = config;
        BarbelHistoContext constructionContext = Optional.ofNullable(BarbelHistoCore.CONSTRUCTION_CONTEXT.get())
                .orElseGet(BarbelHistoBuilder::barbel);
        constructionContext.getContextOptions().put(AdaptingKryoSerializer.OBJECT_TYPE, type);
        constructionContext.getContextOptions().put(AdaptingKryoSerializer.PERSISTENCE_CONFIG, config);
        this.targetSerializer = constructionContext.getPersistenceSerializerProducer().apply(constructionContext);
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
