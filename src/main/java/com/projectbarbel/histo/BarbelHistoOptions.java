package com.projectbarbel.histo;

import java.lang.reflect.Field;

import javax.annotation.Generated;

public class BarbelHistoOptions {

    public final static BarbelHistoOptions DEFAULT_CONFIG = builder().withDefaultValues().build();
    /**
     * Config used when no config is passed to a method or object, i.e. global default config
     */
    public static BarbelHistoOptions ACTIVE_CONFIG = DEFAULT_CONFIG;
    private final String daoClassName;
    private final String serviceClassName;
    private final String idGeneratorClassName;
    private final String pojoCopierClassName;
    private final String updaterClassName;

    @Generated("SparkTools")
    private BarbelHistoOptions(Builder builder) {
        this.daoClassName = builder.daoClassName;
        this.serviceClassName = builder.serviceClassName;
        this.idGeneratorClassName = builder.idGeneratorClassName;
        this.pojoCopierClassName = builder.pojoCopierClassName;
        this.updaterClassName = builder.updaterClassName;
    }

    public String getDaoClassName() {
        return daoClassName;
    }

    public String getServiceClassName() {
        return serviceClassName;
    }

    public String getIdGeneratorClassName() {
        return idGeneratorClassName;
    }

    public String getPojoCopierClassName() {
        return pojoCopierClassName;
    }

    public String getUpdaterClassName() {
        return updaterClassName;
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
        private String daoClassName;
        private String serviceClassName;
        private String idGeneratorClassName;
        private String pojoCopierClassName;
        private String updaterClassName;

        private Builder() {
        }
        
        public Builder withDefaultValues() {
            daoClassName = "com.projectbarbel.histo.dao.DefaultDocumentDao";
            serviceClassName = "com.projectbarbel.histo.service.DefaultDocumentService";
            idGeneratorClassName = "com.projectbarbel.histo.model.DefaultIDGenerator";
            pojoCopierClassName = "com.projectbarbel.histo.model.DefaultPojoCopier";
            updaterClassName = "com.projectbarbel.histo.model.VersionUpdate";
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

        public Builder withIdGeneratorClassName(String idGeneratorClassName) {
            this.idGeneratorClassName = idGeneratorClassName;
            return this;
        }

        public Builder withPojoCopierClassName(String pojoCopierClassName) {
            this.pojoCopierClassName = pojoCopierClassName;
            return this;
        }

        public Builder withUpdaterClassName(String updaterClassName) {
            this.updaterClassName = updaterClassName;
            return this;
        }

        public BarbelHistoOptions build() {
            return new BarbelHistoOptions(this);
        }
    }
    
    public boolean allSet() throws IllegalAccessException {
        for (Field f : getClass().getDeclaredFields())
            if (f.get(this) == null)
                return false;
        return true;            
    }

}
