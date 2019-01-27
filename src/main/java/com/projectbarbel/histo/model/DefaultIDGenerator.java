package com.projectbarbel.histo.model;

import java.util.UUID;
import java.util.function.Supplier;

public class DefaultIDGenerator implements Supplier<String> {

    public static String generateId() {
        return new DefaultIDGenerator().get();
    }
    
    @Override
    public String get() {
        return UUID.randomUUID().toString();
    }
    
}
