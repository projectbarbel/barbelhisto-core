package org.projectbarbel.histo.model;

import java.util.Objects;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelMode;

/**
 * Class used by {@link BarbelHisto} in {@link BarbelMode#POJO} to persist data
 * to persistent storage. Can also be used in {@link BarbelMode#BITEMPORAL} as
 * {@link Bitemporal} container for arbitrary business classes that should be
 * managed be {@link BarbelHisto}.
 * 
 * @author Niklas Schlimm
 *
 */
public class BitemporalVersion implements Bitemporal {

	private BitemporalStamp bitemporalStamp;
	private Object object;
    private String objectType;

	public BitemporalVersion() {
    }
	
    public BitemporalVersion(BitemporalStamp stamp, Object object) {
		super();
		this.bitemporalStamp = stamp;
		this.object = object;
		this.objectType = object.getClass().getName();
	}

	@Override
	public BitemporalStamp getBitemporalStamp() {
		return bitemporalStamp;
	}

	@Override
	public void setBitemporalStamp(BitemporalStamp stamp) {
		this.bitemporalStamp = stamp;
	}

	public Object getObject() {
		return object;
	}

	public String getObjectType() {
		return objectType;
	}

    public void setObject(Object object) {
        this.object = object;
    }

    @Override
    public int hashCode() {
        return Objects.hash(object, objectType, bitemporalStamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BitemporalVersion other = (BitemporalVersion) obj;
        return Objects.equals(object, other.object) && Objects.equals(objectType, other.objectType)
                && Objects.equals(bitemporalStamp, other.bitemporalStamp);
    }

    @Override
	public String toString() {
		return "BitemporalVersion [stamp=" + bitemporalStamp + ", object=" + object + ", objectType=" + objectType + "]";
	}

}
