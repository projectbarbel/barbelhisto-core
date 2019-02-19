package org.projectbarbel.histo.pojos;

import java.util.Objects;

import org.projectbarbel.histo.DocumentId;
import org.projectbarbel.histo.functions.BarbelPojoSerializer;

import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;

@PersistenceConfig(serializer=BarbelPojoSerializer.class, polymorphic=true)
public class PrimitivePrivatePojoPartialContructor {
    @DocumentId
    private String id;
    private boolean someBoolean;
    private byte somByte;
    private short someShort;
    private char someChar;
    private int someInt;
    private float someFloat;
    private long someLong;
    private double someDouble;

    public PrimitivePrivatePojoPartialContructor(String id, boolean someBoolean, char someChar, float someFloat,
            double someDouble) {
        super();
        this.id = id;
        this.someBoolean = someBoolean;
        this.someChar = someChar;
        this.someFloat = someFloat;
        this.someDouble = someDouble;
    }

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
        PrimitivePrivatePojoPartialContructor other = (PrimitivePrivatePojoPartialContructor) obj;
        return Objects.equals(id, other.id) && somByte == other.somByte && someBoolean == other.someBoolean
                && someChar == other.someChar
                && Double.doubleToLongBits(someDouble) == Double.doubleToLongBits(other.someDouble)
                && Float.floatToIntBits(someFloat) == Float.floatToIntBits(other.someFloat) && someInt == other.someInt
                && someLong == other.someLong && someShort == other.someShort;
    }

}
