package com.projectbarbel.histo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.projectbarbel.histo.functions.DefaultIDGenerator;

public class BarbelHistoContextTest {

    @Test
    public void testGetProperty() throws Exception {
        assertEquals("SYSTEM", BarbelHistoContext.CONTEXT.defaultCreatedBy());
    }

    @Test
    public void testVersionIdGenerator() throws Exception {
        assertTrue(BarbelHistoContext.CONTEXT.versionIdGenerator() != null);
    }

    @Test
    public void testDocumentIdGenerator() throws Exception {
        assertTrue(BarbelHistoContext.CONTEXT.documentIdGenerator() instanceof DefaultIDGenerator);
    }

}
