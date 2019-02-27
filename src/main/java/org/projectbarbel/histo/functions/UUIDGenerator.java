package org.projectbarbel.histo.functions;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Default version is generator. Version IDs must be globally unique.
 * 
 * @author Niklas Schlimm
 *
 */
public class UUIDGenerator implements Supplier<String> {

    public static String generateId() {
        return new UUIDGenerator().get();
    }

    @Override
    public String get() {
        return UUID.randomUUID().toString();
    }

}
