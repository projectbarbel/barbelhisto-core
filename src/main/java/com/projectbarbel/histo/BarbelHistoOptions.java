package com.projectbarbel.histo;

import javax.annotation.Generated;

import org.apache.commons.lang3.Validate;

public class BarbelHistoOptions {

    public final static BarbelHistoOptions DEFAULT_CONFIG = builder().withDaoSupplierClassName("com.projectbarbel.histo.dao.DocumentDao$ProxySupplier")
            .withServiceSupplierClassName("com.projectbarbel.histo.service.DocumentService$DocumentServiceProxy").build();
    private final String daoSupplierClassName;
    private final String serviceSupplierClassName;

    @Generated("SparkTools")
    private BarbelHistoOptions(Builder builder) {
        this.daoSupplierClassName = builder.daoSupplierClassName;
        this.serviceSupplierClassName = builder.serviceSupplierClassName;
    }

    public String getDaoSupplierClassName() {
        return daoSupplierClassName;
    }

    public String getServiceSupplierClassName() {
        return serviceSupplierClassName;
    }

    public void validate() {
        Validate.validState(daoSupplierClassName!=null, "daoClassName must not be null");
        Validate.validState(serviceSupplierClassName!=null, "serviceClassName must not be null");
    }

    /**
     * Creates builder to build {@link BarbelHistoOptions}.
     * @return created builder
     */
    @Generated("SparkTools")
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder to build {@link BarbelHistoOptions}.
     */
    @Generated("SparkTools")
    public static final class Builder {
        private String daoSupplierClassName;
        private String serviceSupplierClassName;

        private Builder() {
        }

        public Builder withDaoSupplierClassName(String daoSupplierClassName) {
            this.daoSupplierClassName = daoSupplierClassName;
            return this;
        }

        public Builder withServiceSupplierClassName(String serviceSupplierClassName) {
            this.serviceSupplierClassName = serviceSupplierClassName;
            return this;
        }

        public BarbelHistoOptions build() {
            return new BarbelHistoOptions(this);
        }
    }

}
