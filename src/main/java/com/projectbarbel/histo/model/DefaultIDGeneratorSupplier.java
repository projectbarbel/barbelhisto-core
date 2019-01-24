package com.projectbarbel.histo.model;

import java.util.UUID;
import java.util.function.Supplier;

public class DefaultIDGeneratorSupplier implements Supplier<Supplier<String>> {

    @Override
    public Supplier<String> get() {
        return () -> UUID.randomUUID().toString();
    }
    
}
