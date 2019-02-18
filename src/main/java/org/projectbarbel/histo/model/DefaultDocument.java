package org.projectbarbel.histo.model;

import javax.annotation.Generated;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.DocumentId;
import org.projectbarbel.histo.functions.BarbelPojoSerializer;

import com.googlecode.cqengine.persistence.disk.DiskPersistence;
import com.googlecode.cqengine.persistence.offheap.OffHeapPersistence;
import com.googlecode.cqengine.persistence.support.serialization.PersistenceConfig;

/**
 * A fully equipped example implementation for business classes managed by
 * {@link BarbelHisto}. Notice that the interface {@link Bitemporal} is only
 * required in {@link BarbelMode#BITEMPORAL} and the {@link PersistenceConfig}
 * is only required with {@link DiskPersistence} and {@link OffHeapPersistence}.
 * 
 * @author Niklas Schlimm
 *
 */
@PersistenceConfig(serializer = BarbelPojoSerializer.class, polymorphic = true)
public class DefaultDocument implements Bitemporal {

    @DocumentId
    private String id;
    private BitemporalStamp bitemporalStamp;
    private String data;

    @Generated("SparkTools")
    private DefaultDocument(Builder builder) {
        this.bitemporalStamp = builder.bitemporalStamp;
        this.data = builder.data;
        this.id = builder.id;
    }

    public void setData(String data) {
        this.data = data;
    }

    public DefaultDocument() {
        super();
    }

    public DefaultDocument(String objectId, BitemporalStamp bitemporalStamp, String data) {
        super();
        this.bitemporalStamp = bitemporalStamp;
        this.data = data;
    }

    public DefaultDocument(BitemporalStamp stamp, String data) {
        this.bitemporalStamp = stamp;
        this.data = data;
    }

    public DefaultDocument(DefaultDocument template) {
        this.bitemporalStamp = template.getBitemporalStamp();
        this.data = template.getData();
        this.id = template.getId();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((bitemporalStamp == null) ? 0 : bitemporalStamp.hashCode());
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        DefaultDocument other = (DefaultDocument) obj;
        if (bitemporalStamp == null) {
            if (other.bitemporalStamp != null)
                return false;
        } else if (!bitemporalStamp.equals(other.bitemporalStamp))
            return false;
        if (data == null) {
            if (other.data != null)
                return false;
        } else if (!data.equals(other.data))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public BitemporalStamp getBitemporalStamp() {
        return bitemporalStamp;
    }

    @Override
    public String toString() {
        return "DefaultDocument [bitemporalStamp=" + bitemporalStamp + ", data=" + data + "]";
    }

    @Override
    public void setBitemporalStamp(BitemporalStamp stamp) {
        this.bitemporalStamp = stamp;
    }

    /**
     * Creates builder to build {@link DefaultDocument}.
     * 
     * @return created builder
     */
    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link DefaultDocument}.
     */
    @Generated("SparkTools")
    public static final class Builder {
        private BitemporalStamp bitemporalStamp;
        private String data;
        private String id;

        private Builder() {
        }

        public Builder withBitemporalStamp(BitemporalStamp bitemporalStamp) {
            this.bitemporalStamp = bitemporalStamp;
            return this;
        }

        public Builder withData(String data) {
            this.data = data;
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public DefaultDocument build() {
            return new DefaultDocument(this);
        }
    }

}
