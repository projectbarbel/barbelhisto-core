package com.projectbarbel.histo.dao.mongo;

import java.util.Objects;

import org.bson.types.ObjectId;

import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;

public class DefaultMongoValueObject implements Bitemporal {

	private ObjectId id;
	public void setId(ObjectId id) {
        this.id = id;
    }

    public void setBitemporalStamp(BitemporalStamp bitemporalStamp) {
        this.bitemporalStamp = bitemporalStamp;
    }

    public void setData(String data) {
        this.data = data;
    }

    private BitemporalStamp bitemporalStamp;
	private String data;
	
	public String getData() {
		return data;
	}

	public DefaultMongoValueObject() {
    }
	
	public DefaultMongoValueObject(ObjectId id, BitemporalStamp bitemporalStamp, String data) {
		super();
		this.id = id;
		this.bitemporalStamp = bitemporalStamp;
		this.data = data;
	}

	@Override
	public BitemporalStamp getBitemporalStamp() {
		return bitemporalStamp;
	}

	public ObjectId getId() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof DefaultMongoValueObject)) {
			return false;
		}
		DefaultMongoValueObject defaultValueObject = (DefaultMongoValueObject) o;
		return Objects.equals(data, defaultValueObject.getData())
				&& Objects.equals(bitemporalStamp, defaultValueObject.getBitemporalStamp());
	}

	@Override
	public int hashCode() {
		return Objects.hash(bitemporalStamp, data);
	}

	@Override
	public String toString() {
		return "DefaulMongoValueObject [id=" + id + ", bitemporalStamp=" + bitemporalStamp + ", data=" + data + "]";
	}

}
