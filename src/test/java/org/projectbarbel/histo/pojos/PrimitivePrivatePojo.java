package org.projectbarbel.histo.pojos;

import java.util.Objects;

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
        return Objects.hash(id, somByte, someBoolean, someChar, someDouble, someFloat, someInt, someLong, someShort);
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
        return Objects.equals(id, other.id) && somByte == other.somByte && someBoolean == other.someBoolean
                && someChar == other.someChar
                && Double.doubleToLongBits(someDouble) == Double.doubleToLongBits(other.someDouble)
                && Float.floatToIntBits(someFloat) == Float.floatToIntBits(other.someFloat) && someInt == other.someInt
                && someLong == other.someLong && someShort == other.someShort;
    }
    @Override
    public String toString() {
        return "PrimitivePrivatePojo [id=" + id + ", someBoolean=" + someBoolean + ", somByte=" + somByte
                + ", someShort=" + someShort + ", someChar=" + someChar + ", someInt=" + someInt + ", someFloat="
                + someFloat + ", someLong=" + someLong + ", someDouble=" + someDouble + "]";
    }
	
    
}