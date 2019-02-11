package com.projectbarbel.histo.functions;

import java.util.UUID;
import java.util.function.Supplier;

public class DefaultIDGenerator implements Supplier<Object> {

    public static Object generateId() {
        return new DefaultIDGenerator().get();
    }
    
    @Override
    public Object get() {
        return UUID.randomUUID().toString();
    }
    
}
