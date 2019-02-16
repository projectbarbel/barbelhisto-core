package org.projectbarbel.histo.pojos;

import java.util.List;
import java.util.Map;

import org.projectbarbel.histo.DocumentId;
import org.projectbarbel.histo.functions.BarbelPojoSerializer;

import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;

@PersistenceConfig(serializer=BarbelPojoSerializer.class, polymorphic=true)
public class ComplexFieldsPrivatePojoPartialContructorWithComplexType {
    @DocumentId
    private String id;
	private List<String> stringList;
    private Map<String, NoPrimitivePrivatePojoPartialContructor> someMap;

    public ComplexFieldsPrivatePojoPartialContructorWithComplexType(
            Map<String, NoPrimitivePrivatePojoPartialContructor> someMap) {
        this.someMap = someMap;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((someMap == null) ? 0 : someMap.hashCode());
		result = prime * result + ((stringList == null) ? 0 : stringList.hashCode());
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
		ComplexFieldsPrivatePojoPartialContructorWithComplexType other = (ComplexFieldsPrivatePojoPartialContructorWithComplexType) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (someMap == null) {
			if (other.someMap != null)
				return false;
		} else if (!someMap.equals(other.someMap))
			return false;
		if (stringList == null) {
			if (other.stringList != null)
				return false;
		} else if (!stringList.equals(other.stringList))
			return false;
		return true;
	}
    
}
