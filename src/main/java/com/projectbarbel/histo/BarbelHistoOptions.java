package com.projectbarbel.histo;

import javax.annotation.Generated;

import org.apache.commons.lang3.Validate;

public class BarbelHistoOptions {

    private final static BarbelHistoOptions DEFAULT_CONFIG = builder()
            .withDaoSupplierClassName("com.projectbarbel.histo.dao.DefaultDocumentDao$DefaultDaoSupplier")
            .withServiceSupplierClassName(
                    "com.projectbarbel.histo.service.DefaultDocumentService$DefaultDocumentServiceSupplier")
            .withIdSupplierClassName("com.projectbarbel.histo.model.DefaultIDGeneratorSupplier")
            .withPojoCopierSupplierClassName("com.projectbarbel.histo.model.DefaultPojoCopierSupplier")
            .build();
    /**
     * Config used when no config is passed to a method or object, i.e. global default config
     */
    public static BarbelHistoOptions ACTIVE_CONFIG = DEFAULT_CONFIG;
    private final String daoSupplierClassName;
    private final String serviceSupplierClassName;
    private final String idSupplierClassName;
    private final String pojoCopierSupplierClassName;

    @Generated("SparkTools")
    private BarbelHistoOptions(Builder builder) {
        this.daoSupplierClassName = builder.daoSupplierClassName;
        this.serviceSupplierClassName = builder.serviceSupplierClassName;
        this.idSupplierClassName = builder.idSupplierClassName;
        this.pojoCopierSupplierClassName = builder.pojoCopierSupplierClassName;
    }

    public String getPojoCopierSupplierClassName() {
        return pojoCopierSupplierClassName;
    }

    public String getIdSupplierClassName() {
        return idSupplierClassName;
    }
    
    public String getDaoSupplierClassName() {
        return daoSupplierClassName;
    }

    public String getServiceSupplierClassName() {
        return serviceSupplierClassName;
    }

    public void validate() {
        Validate.validState(daoSupplierClassName != null, "daoClassName must not be null");
        Validate.validState(serviceSupplierClassName != null, "serviceClassName must not be null");
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
        private String idSupplierClassName;
        private String pojoCopierSupplierClassName;

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

        public Builder withIdSupplierClassName(String idSupplierClassName) {
            this.idSupplierClassName = idSupplierClassName;
            return this;
        }

        public Builder withPojoCopierSupplierClassName(String pojoCopierSupplierClassName) {
            this.pojoCopierSupplierClassName = pojoCopierSupplierClassName;
            return this;
        }

        public BarbelHistoOptions build() {
            return new BarbelHistoOptions(this);
        }
    }

}
