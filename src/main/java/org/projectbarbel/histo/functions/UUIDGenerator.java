package org.projectbarbel.histo.functions;

import java.util.UUID;
import java.util.function.Supplier;

public class UUIDGenerator implements Supplier<Object> {

    public static Object generateId() {
        return new UUIDGenerator().get();
    }

    @Override
    public Object get() {
        return UUID.randomUUID().toString();
    }

}
