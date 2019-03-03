package org.projectbarbel.histo.pojos;

import org.projectbarbel.histo.DocumentId;
import org.projectbarbel.histo.functions.BarbelPojoSerializer;

import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@PersistenceConfig(serializer=BarbelPojoSerializer.class, polymorphic=true)
public class Vehicle {

    @DocumentId
    private String id;
    private String serialNumber;
    private String licensenumber;
    private String type;
    private String manufacturer;

}
