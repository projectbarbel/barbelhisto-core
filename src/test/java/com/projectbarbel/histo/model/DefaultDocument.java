package com.projectbarbel.histo.model;

import java.util.Objects;

import javax.annotation.Generated;

public class DefaultDocument implements Bitemporal<String> {

    private BitemporalStamp bitemporalStamp;
    private String data;

    @Generated("SparkTools")
    private DefaultDocument(Builder builder) {
        this.bitemporalStamp = builder.bitemporalStamp;
        this.data = builder.data;
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
        return Objects.equals(data, defaultValueObject.getData())
                && Objects.equals(bitemporalStamp, defaultValueObject.getBitemporalStamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(bitemporalStamp, data);
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

        public DefaultDocument build() {
            return new DefaultDocument(this);
        }
    }

}
