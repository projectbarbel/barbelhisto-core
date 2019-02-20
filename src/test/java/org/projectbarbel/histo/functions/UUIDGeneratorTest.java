package org.projectbarbel.histo.functions;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class UUIDGeneratorTest {

    @Test
    public void testGet() throws Exception {
        String id = (String)new UUIDGenerator().get();
        assertNotNull(id);
    }

    @Test
    public void testGetTwoDifferent() throws Exception {
        String id1 = (String)new UUIDGenerator().get();
        String id2 = (String)new UUIDGenerator().get();
        assertNotEquals(id1, id2);
    }

    @Test
    public void testGenerateId() throws Exception {
        assertNotNull(UUIDGenerator.generateId());
    }

}
