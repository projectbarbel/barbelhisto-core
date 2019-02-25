package org.projectbarbel.histo.pojos;

import java.util.Objects;

import org.projectbarbel.histo.DocumentId;

public class PrimitivePrivatePojoNoPersistence {
	@DocumentId
	public String id;
	public String data;
	
    public PrimitivePrivatePojoNoPersistence() {
    }
	
	public PrimitivePrivatePojoNoPersistence(String id) {
        this.id = id;
	}
	
	public PrimitivePrivatePojoNoPersistence(String id, String data) {
	    this.id = id;
        this.data = data;
	}
	
	public String getData() {
	    return data;
	}
	
	public void setData(String data) {
	    this.data = data;
	}
	
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PrimitivePrivatePojoNoPersistence other = (PrimitivePrivatePojoNoPersistence) obj;
        return Objects.equals(data, other.data) && Objects.equals(id, other.id);
    }
	
}