package org.projectbarbel.histo.model;

import java.util.Objects;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.AbstractBarbelMode;

/**
 * Class used by {@link BarbelHisto} in {@link AbstractBarbelMode#POJO} to persist data
 * to persistent storage.
 * 
 * @author Niklas Schlimm
 *
 * @param <T> the business tabpe to store
 */
public class BitemporalVersion<T> implements Bitemporal {

    private BitemporalStamp stamp;
    private final T object;
    private final String objectType;

    public BitemporalVersion(BitemporalStamp stamp, T object) {
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

    public T getObject() {
        return object;
    }

    public void setStamp(BitemporalStamp stamp) {
        this.stamp = stamp;
    }

    public String getObjectType() {
        return objectType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(object, objectType, stamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof BitemporalVersion)) {
            return false;
        }
        BitemporalVersion<?> other = (BitemporalVersion<?>) obj;
        return Objects.equals(object, other.object) && Objects.equals(objectType, other.objectType)
                && Objects.equals(stamp, other.stamp);
    }

    @Override
    public String toString() {
        return "BitemporalVersion [stamp=" + stamp + ", object=" + object + ", objectType=" + objectType + "]";
    }

}
