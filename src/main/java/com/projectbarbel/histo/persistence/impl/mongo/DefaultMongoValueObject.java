package com.projectbarbel.histo.persistence.impl.mongo;

import java.util.Objects;

import org.bson.types.ObjectId;

import com.projectbarbel.histo.model.Bitemporal;
import com.projectbarbel.histo.model.BitemporalStamp;

public class DefaultMongoValueObject implements Bitemporal<ObjectId> {

    private ObjectId objectId;

    public void setObjectId(ObjectId objectId) {
        this.objectId = objectId;
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
        this.objectId = id;
        this.bitemporalStamp = bitemporalStamp;
        this.data = data;
    }

    @Override
    public BitemporalStamp getBitemporalStamp() {
        return bitemporalStamp;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DefaultMongoValueObject)) {
            return false;
        }
        DefaultMongoValueObject defaultValueObject = (DefaultMongoValueObject) o;
        return Objects.equals(objectId, defaultValueObject.getVersionId())
                && Objects.equals(data, defaultValueObject.getData())
                && Objects.equals(bitemporalStamp, defaultValueObject.getBitemporalStamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, bitemporalStamp, data);
    }

    @Override
    public String toString() {
        return "DefaultMongoValueObject [id=" + objectId + ", bitemporalStamp=" + bitemporalStamp + ", data=" + data + "]";
    }

    @Override
    public ObjectId getVersionId() {
        return objectId;
    }
    
    public ObjectId getId() {
        return objectId;
    }

    public ObjectId getObjectId() {
        return objectId;
    }
    
}
