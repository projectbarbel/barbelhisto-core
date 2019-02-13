package com.projectbarbel.histo.model;

import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;
import com.projectbarbel.histo.BarbelPojoSerializer;

@PersistenceConfig(serializer=BarbelPojoSerializer.class)
public interface Bitemporal {

    BitemporalStamp getBitemporalStamp();

    void setBitemporalStamp(BitemporalStamp stamp);

}
