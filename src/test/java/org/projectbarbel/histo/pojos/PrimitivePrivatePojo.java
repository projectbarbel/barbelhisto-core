package org.projectbarbel.histo.pojos;

import org.projectbarbel.histo.DocumentId;
import org.projectbarbel.histo.functions.BarbelPojoSerializer;

import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;

@PersistenceConfig(serializer=BarbelPojoSerializer.class, polymorphic=true)
public class PrimitivePrivatePojo {
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
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + somByte;
		result = prime * result + (someBoolean ? 1231 : 1237);
		result = prime * result + someChar;
		long temp;
		temp = Double.doubleToLongBits(someDouble);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + Float.floatToIntBits(someFloat);
		result = prime * result + someInt;
		result = prime * result + (int) (someLong ^ (someLong >>> 32));
		result = prime * result + someShort;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrimitivePrivatePojo other = (PrimitivePrivatePojo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (somByte != other.somByte)
			return false;
		if (someBoolean != other.someBoolean)
			return false;
		if (someChar != other.someChar)
			return false;
		if (Double.doubleToLongBits(someDouble) != Double.doubleToLongBits(other.someDouble))
			return false;
		if (Float.floatToIntBits(someFloat) != Float.floatToIntBits(other.someFloat))
			return false;
		if (someInt != other.someInt)
			return false;
		if (someLong != other.someLong)
			return false;
		if (someShort != other.someShort)
			return false;
		return true;
	}

	
}