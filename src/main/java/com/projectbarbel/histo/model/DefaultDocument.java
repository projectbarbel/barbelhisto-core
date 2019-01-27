package com.projectbarbel.histo.model;

import java.util.Objects;
import java.util.UUID;

import javax.annotation.Generated;

public class DefaultDocument implements Bitemporal<String> {

    private String versionId;
    private BitemporalStamp bitemporalStamp;
    private String data;

    public DefaultDocument() {
        super();
    }

    @Generated("SparkTools")
    private DefaultDocument(Builder builder) {
        this.versionId = builder.versionId;
        this.bitemporalStamp = builder.bitemporalStamp;
        this.data = builder.data;
    }

    public DefaultDocument(String objectId, BitemporalStamp bitemporalStamp, String data) {
        super();
        this.versionId = objectId;
        this.bitemporalStamp = bitemporalStamp;
        this.data = data;
    }

    public DefaultDocument(BitemporalStamp stamp, String data) {
        this.versionId = UUID.randomUUID().toString();
        this.bitemporalStamp = stamp;
        this.data = data;
    }

    public DefaultDocument(DefaultDocument template) {
        this.versionId = template.getVersionId();
        this.bitemporalStamp = template.getBitemporalStamp();
        this.data = template.getData();
    }

    public String getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DefaultDocument)) {
            return false;
        }
        DefaultDocument defaultValueObject = (DefaultDocument) o;
        return Objects.equals(versionId, defaultValueObject.getVersionId())
                && Objects.equals(data, defaultValueObject.getData())
                && Objects.equals(bitemporalStamp, defaultValueObject.getBitemporalStamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(versionId, bitemporalStamp, data);
    }

    @Override
    public BitemporalStamp getBitemporalStamp() {
        return bitemporalStamp;
    }

    @Override
    public String toString() {
        return "DefaultDocument [objectId=" + versionId + ", bitemporalStamp=" + bitemporalStamp + ", data=" + data
                + "]";
    }

    @Override
    public String getVersionId() {
        return versionId;
    }

    /**
     * Creates builder to build {@link DefaultDocument}.
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
        private String versionId;
        private BitemporalStamp bitemporalStamp;
        private String data;

        private Builder() {
        }

        public Builder withVersionId(String versionId) {
            this.versionId = versionId;
            return this;
        }

        public Builder withBitemporalStamp(BitemporalStamp bitemporalStamp) {
            this.bitemporalStamp = bitemporalStamp;
            return this;
        }

        public Builder withData(String data) {
            this.data = data;
            return this;
        }

        public DefaultDocument build() {
            return new DefaultDocument(this);
        }
    }

    @Override
    public void setBitemporalStamp(BitemporalStamp stamp) {
        this.bitemporalStamp = stamp;
    }

}
