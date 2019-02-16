package org.projectbarbel.histo.pojos;

import org.projectbarbel.histo.DocumentId;
import org.projectbarbel.histo.functions.BarbelPojoSerializer;

import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;

@PersistenceConfig(serializer=BarbelPojoSerializer.class, polymorphic=true)
public class PrimitivePrivatePojoPartialContructorData {
	@DocumentId
	public String id;
	public boolean someBoolean;
	public byte somByte;
	public short someShort;
	public char someChar;
	public int someInt;
	public float someFloat;
	public long someLong;
	public double someDouble;

	public PrimitivePrivatePojoPartialContructorData() {
	}
}