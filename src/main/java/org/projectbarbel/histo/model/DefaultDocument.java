package org.projectbarbel.histo.model;

import java.util.Objects;

import org.projectbarbel.histo.BarbelHisto;
import org.projectbarbel.histo.BarbelMode;
import org.projectbarbel.histo.DocumentId;

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
public class DefaultDocument implements Bitemporal {

    @DocumentId
    private String id;
    private BitemporalStamp bitemporalStamp;
    private String data;

    public DefaultDocument(String id, String data) {
        super();
        this.id = id;
        this.data = data;
    }

    @Override
    public BitemporalStamp getBitemporalStamp() {
        return bitemporalStamp;
    }
    
    @Override
    public void setBitemporalStamp(BitemporalStamp stamp) {
        this.bitemporalStamp = stamp;
    }

    public DefaultDocument() {
        super();
    }
    
    public DefaultDocument(String objectId, BitemporalStamp bitemporalStamp, String data) {
        super();
        id = objectId;
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
    
    public void setData(String data) {
        this.data = data;
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
    
    private DefaultDocument(Builder builder) {
        this.bitemporalStamp = builder.bitemporalStamp;
        this.data = builder.data;
        this.id = builder.id;
    }

    @Override
    public String toString() {
        return "DefaultDocument [bitemporalStamp=" + bitemporalStamp + ", data=" + data + "]";
    }

    /**
     * Creates builder to build {@link DefaultDocument}.
     * 
     * @return created builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, bitemporalStamp, data);
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
        return Objects.equals(bitemporalStamp, other.bitemporalStamp) && Objects.equals(data, other.data)
                && Objects.equals(id, other.id);
    }

    /**
     * Builder to build {@link DefaultDocument}.
     */
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
