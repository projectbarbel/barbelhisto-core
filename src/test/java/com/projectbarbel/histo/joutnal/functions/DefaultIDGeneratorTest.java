package com.projectbarbel.histo.joutnal.functions;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.projectbarbel.histo.joutnal.functions.DefaultIDGenerator;

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
