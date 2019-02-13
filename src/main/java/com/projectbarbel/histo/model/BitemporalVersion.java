package com.projectbarbel.histo.model;

public class BitemporalVersion implements Bitemporal {

    private BitemporalStamp stamp;
    private final Object object;
    private final String objectType;
    
    public BitemporalVersion(BitemporalStamp stamp, Object object) {
        super();
        this.stamp = stamp;
        this.object = object;
        this.objectType = object.getClass().getName();
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

    @SuppressWarnings("unchecked")
    public <T> T getObject() {
        return (T)object;
    }

    public void setStamp(BitemporalStamp stamp) {
        this.stamp = stamp;
    }

    public String getObjectType() {
        return objectType;
    }

}
