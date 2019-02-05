package com.projectbarbel.histo.journal.functions;

import java.io.Serializable;
import java.util.UUID;
import java.util.function.Supplier;

public class DefaultIDGenerator implements Supplier<Serializable> {

    public static Serializable generateId() {
        return new DefaultIDGenerator().get();
    }
    
    @Override
    public Serializable get() {
        return UUID.randomUUID().toString();
    }
    
}
