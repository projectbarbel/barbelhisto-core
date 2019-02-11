package com.projectbarbel.histo.model;

public class BitemporalVersion implements Bitemporal {

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

    @SuppressWarnings("unchecked")
    public <T> T getObject() {
        return (T)object;
    }

    public void setStamp(BitemporalStamp stamp) {
        this.stamp = stamp;
    }

    public void setObject(Object object) {
        this.object = object;
    }

}
