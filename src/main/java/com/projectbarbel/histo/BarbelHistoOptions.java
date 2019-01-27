package com.projectbarbel.histo;

import java.lang.reflect.Field;

import javax.annotation.Generated;

public class BarbelHistoOptions {

    private final String daoClassName;
    private final String serviceClassName;
    private final String idGeneratorClassName;
    private final String pojoCopierClassName;
    private final String updaterClassName;
    private final String periodicUpdateStrategyClassName;

    @Generated("SparkTools")
    private BarbelHistoOptions(Builder builder) {
        this.daoClassName = builder.daoClassName;
        this.serviceClassName = builder.serviceClassName;
        this.idGeneratorClassName = builder.idGeneratorClassName;
        this.pojoCopierClassName = builder.pojoCopierClassName;
        this.updaterClassName = builder.updaterClassName;
        this.periodicUpdateStrategyClassName = builder.periodicUpdateStrategyClassName;
    }

    public final static BarbelHistoOptions withDaoClassName(String customDaoClassName) {
        return builderWithDefaultValues().withDaoClassName(customDaoClassName).build();
    }

    public static BarbelHistoOptions withDefaultValues() {
        return builderWithDefaultValues().build();
    }

    public String getUpdatePolicyClassName() {
        return periodicUpdateStrategyClassName;
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

    public static Builder builderWithDefaultValues() {
        Builder builder = new Builder();
        builder.daoClassName = "com.projectbarbel.histo.persistence.impl.DefaultDocumentDao";
        builder.serviceClassName = "com.projectbarbel.histo.persistence.impl.DefaultDocumentService";
        builder.idGeneratorClassName = "com.projectbarbel.histo.model.DefaultIDGenerator";
        builder.pojoCopierClassName = "com.projectbarbel.histo.model.DefaultPojoCopier";
        builder.updaterClassName = "com.projectbarbel.histo.model.VersionUpdate";
        builder.periodicUpdateStrategyClassName = "com.projectbarbel.histo.model.KeepSubsequentUpdateStrategy";
        return builder;
    }

    public boolean allSet() throws IllegalAccessException {
        for (Field f : getClass().getDeclaredFields())
            if (f.get(this) == null)
                return false;
        return true;
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
        private String periodicUpdateStrategyClassName;

        private Builder() {
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

        public Builder withPeriodicUpdateStrategyClassName(String periodicUpdateStrategyClassName) {
            this.periodicUpdateStrategyClassName = periodicUpdateStrategyClassName;
            return this;
        }

        public BarbelHistoOptions build() {
            return new BarbelHistoOptions(this);
        }
    }

}
