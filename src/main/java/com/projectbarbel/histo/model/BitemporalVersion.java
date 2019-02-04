package com.projectbarbel.histo.model;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

public class BitemporalVersion implements Bitemporal<BitemporalVersion> {

    private BitemporalStamp stamp;
    private Object object;
    
    public BitemporalVersion(BitemporalStamp stamp, Object object) {
        super();
        this.stamp = stamp;
        this.object = object;
    }

    @Override
    public BitemporalStamp getBitemporalStamp() {
        return stamp;
    }
    
    @Override
    public void setBitemporalStamp(BitemporalStamp stamp) {
        this.stamp = stamp;
    }

    public BitemporalStamp getStamp() {
        return stamp;
    }

    public Object getObject() {
        return object;
    }

    public void setStamp(BitemporalStamp stamp) {
        this.stamp = stamp;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public static final Attribute<BitemporalVersion, String> DOCUMENT_ID = new SimpleAttribute<BitemporalVersion, String>("documentId") {
        public String getValue(BitemporalVersion object, QueryOptions queryOptions) { return object.getDocumentId(); }
    };
    
}
