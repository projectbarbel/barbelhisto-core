package com.projectbarbel.histo.model;

import java.util.UUID;
import java.util.function.Supplier;

public class DefaultIDGenerator implements Supplier<String> {

    public static class DefaultIDGeneratorSupplier implements Supplier<DefaultIDGenerator> {
        @Override
        public DefaultIDGenerator get() {
            return new DefaultIDGenerator();
        }        
    }

    @Override
    public String get() {
        return UUID.randomUUID().toString();
    }
    
}
