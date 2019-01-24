package com.projectbarbel.histo.model;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class DefaultIDGeneratorTest {

    @Test
    public void testGet() throws Exception {
        String id = new DefaultIDGenerator().get();
        assertNotNull(id);
    }

    @Test
    public void testGetTwoDifferent() throws Exception {
        String id1 = new DefaultIDGenerator().get();
        String id2 = new DefaultIDGenerator().get();
        assertNotEquals(id1, id2);
    }

}
