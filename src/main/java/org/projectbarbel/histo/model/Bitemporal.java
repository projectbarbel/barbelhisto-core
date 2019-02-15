package org.projectbarbel.histo.model;

import org.projectbarbel.histo.functions.BarbelPojoSerializer;

import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;

@PersistenceConfig(serializer=BarbelPojoSerializer.class)
public interface Bitemporal {

    BitemporalStamp getBitemporalStamp();

    void setBitemporalStamp(BitemporalStamp stamp);

}
