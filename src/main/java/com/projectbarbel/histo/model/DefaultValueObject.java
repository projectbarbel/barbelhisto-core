package com.projectbarbel.histo.model;

import java.util.Objects;
import java.util.UUID;
import javax.annotation.Generated;

public class DefaultValueObject implements Bitemporal<String> {

    private final String objectId;
    private final BitemporalStamp bitemporalStamp;
    private final String data;

    @Generated("SparkTools")
    private DefaultValueObject(Builder builder) {
        this.objectId = builder.objectId;
        this.bitemporalStamp = builder.bitemporalStamp;
        this.data = builder.data;
    }

    public DefaultValueObject(String objectId, BitemporalStamp bitemporalStamp, String data) {
        super();
        this.objectId = objectId;
        this.bitemporalStamp = bitemporalStamp;
        this.data = data;
    }

    public DefaultValueObject(BitemporalStamp stamp, String data) {
        this.objectId = UUID.randomUUID().toString();
        this.bitemporalStamp = stamp;
        this.data = data;
    }

    public DefaultValueObject(DefaultValueObject template) {
        this.objectId = template.getObjectId();
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
        if (!(o instanceof DefaultValueObject)) {
            return false;
        }
        DefaultValueObject defaultValueObject = (DefaultValueObject) o;
        return Objects.equals(objectId, defaultValueObject.getObjectId())
                && Objects.equals(data, defaultValueObject.getData())
                && Objects.equals(bitemporalStamp, defaultValueObject.getBitemporalStamp());
    }

    @Override
    public int hashCode() {
        return Objects.hash(objectId, bitemporalStamp, data);
    }

    @Override
    public BitemporalStamp getBitemporalStamp() {
        return bitemporalStamp;
    }

    @Override
    public String toString() {
        return "DefaultValueObject [objectId=" + objectId + ", bitemporalStamp=" + bitemporalStamp + ", data=" + data
                + "]";
    }

    @Override
    public String getObjectId() {
        return objectId;
    }

    /**
     * Creates builder to build {@link DefaultValueObject}.
     * @return created builder
     */
    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link DefaultValueObject}.
     */
    @Generated("SparkTools")
    public static final class Builder {
        private String objectId;
        private BitemporalStamp bitemporalStamp;
        private String data;

        private Builder() {
        }

        public Builder withObjectId(String objectId) {
            this.objectId = objectId;
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

        public DefaultValueObject build() {
            return new DefaultValueObject(this);
        }
    }

}
