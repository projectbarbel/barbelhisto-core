package com.projectbarbel.histo;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Generated;

import org.apache.commons.lang3.Validate;

public class BarbelHistoOptions {

    public static final String DAO_SUPPLIER_CLASSNAME = "com.projectbarbel.histo.dao.supplier.classname";
    public static final String SERVICE_SUPPLIER_CLASSNAME = "com.projectbarbel.histo.service.supplier.classname";

    public final static BarbelHistoOptions DEFAULT_CONFIG = builder().withDaoClassName("com.projectbarbel.histo.dao.DocumentDao$ProxySupplier")
            .withServiceClassName("com.projectbarbel.histo.service.DocumentService$DocumentServiceProxy").build();
    private final Map<String, String> options;
    private final String daoClassName;
    private final String serviceClassName;

    @Generated("SparkTools")
    private BarbelHistoOptions(Builder builder) {
        this.options = builder.options;
        this.daoClassName = builder.daoClassName;
        this.serviceClassName = builder.serviceClassName;
    }

    public String getDaoClassName() {
        return daoClassName;
    }

    public String getServiceClassName() {
        return serviceClassName;
    }

    public void addOption(String key, String value) {
        options.put(key, value);
    }

    public Optional<String> getOption(String key) {
        return Optional.ofNullable(options.get(key));
    }

    /**
     * Creates builder to build {@link BarbelHistoOptions}.
     * 
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
        private Map<String, String> options = Collections.emptyMap();
        private String daoClassName;
        private String serviceClassName;

        private Builder() {
        }

        public Builder withOptions(Map<String, String> options) {
            this.options = options;
            return this;
        }

        public Builder withDaoClassName(String daoClassName) {
            this.daoClassName = daoClassName;
            return this;
        }

        public Builder withServiceClassName(String serviceClassName) {
            this.serviceClassName = serviceClassName;
            return this;
        }

        public BarbelHistoOptions build() {
            return new BarbelHistoOptions(this);
        }
    }

    public void validate() {
        Validate.validState(daoClassName!=null, "daoClassName must not be null");
        Validate.validState(serviceClassName!=null, "serviceClassName must not be null");
    }

}
