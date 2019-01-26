package com.projectbarbel.histo.model;

import java.util.UUID;
import java.util.function.Supplier;

public class DefaultIDGenerator implements Supplier<String> {

    @Override
    public String get() {
        return UUID.randomUUID().toString();
    }
    
}
