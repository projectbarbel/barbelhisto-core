package com.projectbarbel.histo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.projectbarbel.histo.functions.DefaultIDGenerator;

public class BarbelHistoContextTest {

    @Test
    public void testGetProperty() throws Exception {
        assertEquals("SYSTEM", BarbelHistoContext.instance().defaultCreatedBy());
    }

    @Test
    public void testVersionIdGenerator() throws Exception {
        assertTrue(BarbelHistoContext.instance().versionIdGenerator() != null);
    }

    @Test
    public void testDocumentIdGenerator() throws Exception {
        assertTrue(BarbelHistoContext.instance().documentIdGenerator() instanceof DefaultIDGenerator);
    }

}
