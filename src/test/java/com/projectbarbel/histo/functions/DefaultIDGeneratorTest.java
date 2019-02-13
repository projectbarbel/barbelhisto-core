package com.projectbarbel.histo.functions;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class DefaultIDGeneratorTest {

    @Test
    public void testGet() throws Exception {
        String id = (String)new DefaultIDGenerator().get();
        assertNotNull(id);
    }

    @Test
    public void testGetTwoDifferent() throws Exception {
        String id1 = (String)new DefaultIDGenerator().get();
        String id2 = (String)new DefaultIDGenerator().get();
        assertNotEquals(id1, id2);
    }

}
